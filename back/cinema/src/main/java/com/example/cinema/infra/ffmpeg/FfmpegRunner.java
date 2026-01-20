package com.example.cinema.infra.ffmpeg;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FfmpegRunner {

    private static final Logger log = LoggerFactory.getLogger(FfmpegRunner.class);

    @Value("${ffmpeg.binary:ffmpeg}")
    private String ffmpegBinary;

    @Value("${ffmpeg.hls_segment_seconds}")
    private int segmentSeconds;

    public void transcodeToHls(Path hostJobDir, Path hostInputMp4, Path hostOutDir) {
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegBinary);
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(hostInputMp4.toAbsolutePath().toString());
        cmd.add("-c:v");
        cmd.add("h264");
        cmd.add("-c:a");
        cmd.add("aac");
        cmd.add("-f");
        cmd.add("hls");
        cmd.add("-hls_time");
        cmd.add(String.valueOf(segmentSeconds));
        cmd.add("-hls_playlist_type");
        cmd.add("vod");
        cmd.add("-hls_segment_filename");
        cmd.add(hostOutDir.resolve("seg_%05d.ts").toAbsolutePath().toString());
        cmd.add(hostOutDir.resolve("index.m3u8").toAbsolutePath().toString());

        run(cmd);
    }

    private void run(List<String> cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            StringBuilder out = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    out.append(line).append("\n");
                }
            }

            int code = p.waitFor();
            if (code != 0) {
                log.error("ffmpeg failed. exit={} output=\n{}", code, out);
                throw new IllegalStateException("ffmpeg failed. exit=" + code);
            }
            log.info("ffmpeg success");
        } catch (Exception e) {
            throw new IllegalStateException("ffmpeg execution error: " + e.getMessage(), e);
        }
    }
}

