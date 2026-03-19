package com.example.cinema.infra.ffmpeg;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "ffmpeg.mode", havingValue = "docker")
@RequiredArgsConstructor
public class FfmpegDockerRunner implements FfmpegTranscoder {

    private static final Logger log = LoggerFactory.getLogger(FfmpegDockerRunner.class);

    @Value("${ffmpeg.docker_image:jrottenberg/ffmpeg:6.1-alpine}")
    private String dockerImage;

    @Value("${ffmpeg.hls_segment_seconds}")
    private int segmentSeconds;

    @Override
    public void transcodeToHls(Path hostJobDir, Path hostInputMp4, Path hostOutDir) {
        String inputInContainer = "/work/" + hostInputMp4.getFileName();
        String outDirInContainer = "/work/" + hostOutDir.getFileName();

        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("-v");
        cmd.add(hostJobDir.toAbsolutePath() + ":/work");
        cmd.add("-w");
        cmd.add("/work");
        cmd.add(dockerImage);
        cmd.add("-y");
        cmd.add("-hide_banner");
        cmd.add("-loglevel");
        cmd.add("error");
        cmd.add("-i");
        cmd.add(inputInContainer);
        cmd.add("-c:v");
        cmd.add("libx264");
        cmd.add("-c:a");
        cmd.add("aac");
        cmd.add("-f");
        cmd.add("hls");
        cmd.add("-hls_time");
        cmd.add(String.valueOf(segmentSeconds));
        cmd.add("-hls_playlist_type");
        cmd.add("vod");
        cmd.add("-hls_segment_filename");
        cmd.add(outDirInContainer + "/seg_%05d.ts");
        cmd.add(outDirInContainer + "/index.m3u8");

        log.info("ffmpeg docker cmd={}", String.join(" ", cmd));
        try {
            boolean inputExists = Files.exists(hostInputMp4);
            long inputSize = inputExists ? Files.size(hostInputMp4) : -1L;
            log.info("ffmpeg docker input exists={} size={} path={}", inputExists, inputSize, hostInputMp4);
            log.info("ffmpeg docker outDir exists={} path={}", Files.exists(hostOutDir), hostOutDir);
        } catch (Exception e) {
            log.warn("ffmpeg docker precheck failed: {}", e.getMessage());
        }

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
                log.error("ffmpeg docker failed. exit={} output=\n{}", code, out);
                throw new IllegalStateException("ffmpeg docker failed. exit=" + code + " output=\n" + out);
            }
            log.info("ffmpeg docker success");
        } catch (Exception e) {
            throw new IllegalStateException("ffmpeg docker execution error: " + e.getMessage(), e);
        }
    }
}
