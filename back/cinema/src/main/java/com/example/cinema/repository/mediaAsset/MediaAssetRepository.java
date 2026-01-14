package com.example.cinema.repository.mediaAsset;

import com.example.cinema.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
}
