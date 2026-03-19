package com.example.cinema.infra.ffmpeg;

import java.nio.file.Path;

public interface FfmpegTranscoder {
    void transcodeToHls(Path hostJobDir, Path hostInputMp4, Path hostOutDir);
}