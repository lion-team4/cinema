package com.example.cinema.asset.transcode;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class TranscodeService {

    private final TranscodeJobRepository repo;
    private final S3Storage s3;
    private final FfmpegRunner ffmpeg;

    @Value("${transcode.workdir:/tmp/work}")
    private String workdir;

    /**
     * 업로드 완료 콜백에서 호출.
     * 중복 호출되어도 tryMarkTranscoding()로 1번만 실행됨.
     */
    @Transactional
    public boolean requestTranscode(Long movieId, String sourceKey) {
        String hlsPrefix = "hls/test/movie" + movieId + "/";

        TranscodeJob job = repo.findById(movieId).orElseGet(() ->
                TranscodeJob.builder()
                        .movieId(movieId)
                        .sourceKey(sourceKey)
                        .hlsPrefix(hlsPrefix)
                        .status(TranscodeStatus.UPLOADED)
                        .build()
        );

        job.markUploaded(sourceKey, hlsPrefix);
        repo.save(job);

        int updated = repo.tryMarkTranscoding(movieId);
        if (updated == 1) {
            runTranscodeAsync(movieId);
            return true;
        }
        return false;
    }

    @Async
    public void runTranscodeAsync(Long movieId) {
        try {
            doTranscode(movieId);
        } catch (Exception e) {
            markFailed(movieId, e);
        }
    }

    @Transactional
    protected void doTranscode(Long movieId) throws Exception {
        TranscodeJob job = repo.findById(movieId)
                .orElseThrow(() -> new IllegalStateException("job not found: " + movieId));

        Path movieDir = Paths.get(workdir, "movie" + movieId);
        Path input = movieDir.resolve("source.mp4");
        Path outDir = movieDir.resolve("out");

        // 1) S3 uploads -> 로컬 파일
        s3.downloadToFile(job.getSourceKey(), input);

        // 2) ffmpeg 변환
        Files.createDirectories(outDir);
        ffmpeg.transcodeToHls(input, outDir);

        // 3) out/* -> S3 hlsPrefix 업로드(헤더 포함)
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(outDir)) {
            for (Path p : stream) {
                if (!Files.isRegularFile(p)) continue;
                String key = job.getHlsPrefix() + p.getFileName().toString();
                s3.uploadFileWithHeaders(p, key);
            }
        }

        // 4) READY
        job.markReady();
        repo.save(job);

        // 5) 디스크 정리
        cleanupDir(movieDir);
    }

    @Transactional
    protected void markFailed(Long movieId, Exception e) {
        repo.findById(movieId).ifPresent(job -> {
            job.markFailed(e.getMessage());
            repo.save(job);
        });
    }

    private void cleanupDir(Path dir) {
        try {
            if (!Files.exists(dir)) return;
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {
        }
    }
}

