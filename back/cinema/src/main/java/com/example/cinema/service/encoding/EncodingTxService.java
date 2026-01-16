package com.example.cinema.service.encoding;

import com.example.cinema.entity.Content;
import com.example.cinema.entity.MediaAsset;
import com.example.cinema.exception.BusinessException;
import com.example.cinema.exception.ErrorCode;
import com.example.cinema.repository.content.ContentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EncodingTxService {

    private final ContentRepository contentRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void linkHlsMaster(long contentId, long hlsAssetId) {
        Content content = contentRepository.findByIdForUpdate(contentId)
                .orElseThrow(() -> new BusinessException("콘텐츠를 찾을 수 없습니다. ID: " + contentId, ErrorCode.CONTENT_NOT_FOUND));

        if (content.getVideoHlsMaster() != null) return;

        MediaAsset ref = em.getReference(MediaAsset.class, hlsAssetId);
        content.attachAssets(null, null, ref);
    }

    @Transactional
    public void markReady(long contentId) {
        Content content = contentRepository.findByIdForUpdate(contentId)
                .orElseThrow(() -> new BusinessException("콘텐츠를 찾을 수 없습니다. ID: " + contentId, ErrorCode.CONTENT_NOT_FOUND));
        content.markEncodingReady();
    }

    @Transactional
    public void markFailed(long contentId, String error) {
        Content content = contentRepository.findByIdForUpdate(contentId)
                .orElseThrow(() -> new BusinessException("콘텐츠를 찾을 수 없습니다. ID: " + contentId, ErrorCode.CONTENT_NOT_FOUND));
        content.markEncodingFailed(trim(error));
    }

    private String trim(String error) {
        if (error == null) return null;
        return error.length() <= 2000 ? error : error.substring(0, 2000);
    }
}