package com.example.cinema.service.theater;

import com.example.cinema.dto.theater.PlaybackInfoResponse;
import com.example.cinema.entity.Content;
import com.example.cinema.entity.MediaAsset;
import com.example.cinema.entity.ScheduleItem;
import com.example.cinema.entity.User;
import com.example.cinema.repository.schedule.ScheduleItemRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.service.media.CloudFrontUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TheaterPlaybackService {

    private final ScheduleItemRepository scheduleItemRepository;
    private final CloudFrontUrlService cloudFrontUrlService;
    private final UserRepository userRepository;

    public PlaybackInfoResponse getPlaybackInfo(long scheduleId, User detachedUser) {
        // 영속성 컨텍스트 내에서 User 다시 조회 (Lazy Loading 문제 해결)
        User user = userRepository.findById(detachedUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 구독 상태 검증
        if (user.getSubscription() == null || !user.getSubscription().getIsActive()) {
            throw new AccessDeniedException("구독이 필요한 서비스입니다.");
        }

        // 스케줄 조회
        ScheduleItem scheduleItem = scheduleItemRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));

        Content content = scheduleItem.getContent();
        if (content == null) {
            return null;
        }

        // HLS 우선, 없으면 원본 영상
        MediaAsset asset = content.getVideoHlsMaster();
        if (asset == null) {
            asset = content.getVideoSource();
        }
        if (asset == null) {
            return null;
        }

        String url = cloudFrontUrlService.toPublicUrl(asset.getObjectKey());

        return PlaybackInfoResponse.from(asset, url);
    }
}
