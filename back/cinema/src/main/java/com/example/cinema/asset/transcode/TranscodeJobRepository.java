package com.example.cinema.asset.transcode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TranscodeJobRepository extends JpaRepository<TranscodeJob, Long> {

    // /complete가 2번 와도 한 번만 TRANSCODING으로 넘어가게 하는 "원자적 장치"
    @Modifying
    @Query("""
        update TranscodeJob j
           set j.status = com.example.cinema.asset.transcode.TranscodeStatus.TRANSCODING,
               j.startedAt = CURRENT_TIMESTAMP,
               j.errorMessage = null
         where j.movieId = :movieId
           and j.status in (com.example.cinema.asset.transcode.TranscodeStatus.UPLOADED,
                            com.example.cinema.asset.transcode.TranscodeStatus.FAILED)
    """)
    int tryMarkTranscoding(Long movieId);
}

