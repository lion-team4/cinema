package com.example.cinema.service.encoding;

import com.example.cinema.exception.BusinessException;
import com.example.cinema.exception.ErrorCode;
import com.example.cinema.infra.ffmpeg.FfmpegDockerRunner;
import com.example.cinema.infra.s3.S3ObjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HlsTranscodeService {

    private final S3ObjectService s3ObjectService;
    private final FfmpegDockerRunner ffmpeg;

    @Value("${ffmpeg.work_dir}")
    private String workDir;

    public record HlsTranscodeResult(int segmentCount, long durationMs) {
    }

    public HlsTranscodeResult transcodeAndUpload(long contentId,
                                                 String sourceKey,
                                                 String masterKey,
                                                 long minBytesVideo) {

        Path jobDir = Paths.get(workDir).resolve("content-" + contentId);
        Path input = jobDir.resolve("source.mp4");
        Path outDir = jobDir.resolve("out");

        try {
            Files.createDirectories(outDir);

            s3ObjectService.assertReady(sourceKey, minBytesVideo, "video/", 3, new long[]{300, 600, 1200});
            s3ObjectService.downloadToFile(sourceKey, input);

            ffmpeg.transcodeToHls(jobDir, input, outDir);

            List<Path> tsFiles = Files.list(outDir)
                    .filter(p -> p.getFileName().toString().endsWith(".ts"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();

            for (Path ts : tsFiles) {
                String key = "hls/" + contentId + "/" + ts.getFileName();
                s3ObjectService.uploadFile(key, ts, "video/mp2t", "public, max-age=31536000, immutable");
            }

            Path m3u8 = outDir.resolve("index.m3u8");
            if (!Files.exists(m3u8)) {
                throw new BusinessException("변환 결과 파일(index.m3u8)을 찾을 수 없습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
            }

            s3ObjectService.uploadFile(masterKey, m3u8, "application/vnd.apple.mpegurl", "public, max-age=60");

            long durationMs = parseDurationMs(m3u8);
            return new HlsTranscodeResult(tsFiles.size(), durationMs);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("HLS 변환 작업 중 오류가 발생했습니다: " + e.getMessage(), ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            safeDelete(jobDir);
        }
    }

    private long parseDurationMs(Path m3u8) throws IOException {
        double totalSeconds = 0.0;
        for (String line : Files.readAllLines(m3u8)) {
            if (!line.startsWith("#EXTINF:")) {
                continue;
            }
            int comma = line.indexOf(',');
            String raw = line.substring(8, comma > 0 ? comma : line.length()).trim();
            if (!raw.isEmpty()) {
                totalSeconds += Double.parseDouble(raw);
            }
        }
        return Math.max(0L, Math.round(totalSeconds * 1000));
    }

    private void safeDelete(Path dir) {
        try {
            if (!Files.exists(dir)) return;
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }
}