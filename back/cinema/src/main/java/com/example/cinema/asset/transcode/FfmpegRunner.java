package com.example.cinema.asset.transcode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class FfmpegRunner {

    @Value("${transcode.ffmpeg.dockerImage}")
    private String dockerImage;

    @Value("${transcode.ffmpeg.segmentSeconds:6}")
    private int segmentSeconds;

    /**
     * inputFile: /tmp/work/movie1/source.mp4
     * outputDir: /tmp/work/movie1/out
     */
    public void transcodeToHls(Path inputFile, Path outputDir) throws IOException, InterruptedException {
        Path workDir = inputFile.getParent();         // /tmp/work/movie1
        Path inContainerInput = Path.of("/work").resolve(inputFile.getFileName()); // /work/source.mp4
        Path inContainerOutIndex = Path.of("/work").resolve("out").resolve("index.m3u8");

        // NOTE:
        // - 가장 정석: docker를 sudo 없이 실행 가능하게 만들고(권장) 아래 cmd 사용
        // - 현재 환경에서 sudo docker만 되면, 아래에서 sudo를 붙여야 함(운영 비권장)
        List<String> cmd = new ArrayList<>();
        cmd.add("docker"); // 권장(비-sudo)
        // cmd.add("sudo"); cmd.add("docker"); // 필요하면 이 줄로 바꿔서 사용

        cmd.addAll(List.of(
                "run", "--rm",
                "-v", workDir.toAbsolutePath() + ":/work",
                dockerImage,
                "ffmpeg",
                "-y",
                "-i", inContainerInput.toString(),
                "-codec:v", "h264",
                "-codec:a", "aac",
                "-hls_time", String.valueOf(segmentSeconds),
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", "/work/out/seg_%05d.ts",
                inContainerOutIndex.toString()
        ));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.inheritIO(); // MVP: 로그를 그대로 터미널/로그로

        Process p = pb.start();
        boolean ok = p.waitFor(30, TimeUnit.MINUTES);
        if (!ok) {
            p.destroyForcibly();
            throw new RuntimeException("ffmpeg timed out (30m)");
        }
        int code = p.exitValue();
        if (code != 0) {
            throw new RuntimeException("ffmpeg failed, exitCode=" + code);
        }
    }
}

