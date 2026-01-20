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

    @Value("${ffmpeg.mode:local}")
    private String mode; // "local" or "docker"

    @Value("${ffmpeg.binary:ffmpeg}")
    private String ffmpegBinary;

    @Value("${ffmpeg.docker_image:jrottenberg/ffmpeg:6.1-alpine}")
    private String dockerImage;

    @Value("${ffmpeg.hls_segment_seconds}")
    private int segmentSeconds;

    public void transcodeToHls(Path hostJobDir, Path hostInputMp4, Path hostOutDir) {
        List<String> cmd;
        
        if ("docker".equalsIgnoreCase(mode)) {
            cmd = buildDockerCommand(hostJobDir, hostInputMp4, hostOutDir);
        } else {
            cmd = buildLocalCommand(hostInputMp4, hostOutDir);
        }

        run(cmd);
    }

    private List<String> buildDockerCommand(Path hostJobDir, Path hostInputMp4, Path hostOutDir) {
        // 컨테이너 내부 경로
        String containerWorkDir = "/work";
        String containerInput = containerWorkDir + "/" + hostInputMp4.getFileName();
        String containerOutDir = containerWorkDir + "/out";

        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("-v");
        cmd.add(hostJobDir.toAbsolutePath() + ":" + containerWorkDir);
        cmd.add(dockerImage);
        
        // ffmpeg 옵션
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(containerInput);
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
        cmd.add(containerOutDir + "/seg_%05d.ts");
        cmd.add(containerOutDir + "/index.m3u8");

        return cmd;
    }

    private List<String> buildLocalCommand(Path hostInputMp4, Path hostOutDir) {
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

        return cmd;
    }

    private void run(List<String> cmd) {
        log.info("Executing: {}", String.join(" ", cmd));
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

