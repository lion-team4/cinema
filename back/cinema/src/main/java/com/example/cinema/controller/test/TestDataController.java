package com.example.cinema.controller.test;

import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.entity.*;
import com.example.cinema.repository.content.ContentRepository;
import com.example.cinema.repository.mediaAsset.MediaAssetRepository;
import com.example.cinema.repository.schedule.ScheduleDayRepository;
import com.example.cinema.repository.schedule.ScheduleItemRepository;
import com.example.cinema.repository.subscription.SubscriptionRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.type.AssetType;
import com.example.cinema.type.ContentStatus;
import com.example.cinema.type.ScheduleStatus;
import com.example.cinema.type.SubscriptionStatus;
import com.example.cinema.type.Visibility;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 테스트용 데이터 생성 컨트롤러
 * - dev 또는 test 프로필에서만 활성화
 * - 프로덕션에서는 절대 활성화하면 안 됨!
 */
@RestController
@RequestMapping("/test/data")
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class TestDataController {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ContentRepository contentRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 테스트용 구독자 생성 (구독 활성화 상태)
     * GET /test/data/create-subscriber?email=test@test.com&nickname=tester&password=12345678
     */
    @GetMapping("/create-subscriber")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSubscriber(
            @RequestParam(defaultValue = "subscriber@test.com") String email,
            @RequestParam(defaultValue = "구독자") String nickname,
            @RequestParam(defaultValue = "12345678") String password) {

        // 이미 존재하면 삭제 후 재생성
        userRepository.findByEmail(email).ifPresent(u -> {
            if (u.getSubscription() != null) {
                subscriptionRepository.delete(u.getSubscription());
            }
            userRepository.delete(u);
        });

        // 유저 생성
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .passwordHash(passwordEncoder.encode(password))
                .seller(false)
                .build();
        user = userRepository.save(user);

        // 구독 생성 (BillingKey 없이, isActive=true)
        Subscription subscription = Subscription.builder()
                .subscriber(user)
                .name("테스트 구독")
                .price(0L)
                .status(SubscriptionStatus.ACTIVE)
                .isActive(true)
                .currentPeriodStart(LocalDateTime.now())
                .currentPeriodEnd(LocalDateTime.now().plusMonths(1))
                .billingKey(null)
                .build();
        subscriptionRepository.save(subscription);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("email", user.getEmail());
        result.put("nickname", user.getNickname());
        result.put("subscriptionId", subscription.getSubscriptionId());
        result.put("subscriptionActive", subscription.getIsActive());
        result.put("message", "비밀번호: " + password);

        return ResponseEntity.ok(ApiResponse.success("테스트 구독자 생성 완료", result));
    }

    /**
     * 테스트용 판매자(셀러) 생성
     * GET /test/data/create-seller?email=seller@test.com&nickname=셀러&password=12345678
     */
    @GetMapping("/create-seller")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSeller(
            @RequestParam(defaultValue = "seller@test.com") String email,
            @RequestParam(defaultValue = "셀러") String nickname,
            @RequestParam(defaultValue = "12345678") String password) {

        userRepository.findByEmail(email).ifPresent(userRepository::delete);

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .passwordHash(passwordEncoder.encode(password))
                .seller(true)
                .build();
        user = userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("email", user.getEmail());
        result.put("nickname", user.getNickname());
        result.put("isSeller", user.getSeller());
        result.put("message", "비밀번호: " + password);

        return ResponseEntity.ok(ApiResponse.success("테스트 셀러 생성 완료", result));
    }

    /**
     * 테스트용 콘텐츠 + 스케줄 생성 (바로 PLAYING 상태)
     * GET /test/data/create-playing-schedule?sellerEmail=seller@test.com
     * 
     * @param hlsObjectKey S3 HLS 마스터 파일 경로 (예: videos/123/hls/master.m3u8)
     * @param bucket S3 버킷 이름 (기본: test-bucket)
     */
    @GetMapping("/create-playing-schedule")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPlayingSchedule(
            @RequestParam(defaultValue = "seller@test.com") String sellerEmail,
            @RequestParam(defaultValue = "테스트 영화") String title,
            @RequestParam(defaultValue = "60") int durationMinutes,
            @RequestParam(defaultValue = "test/hls/master.m3u8") String hlsObjectKey,
            @RequestParam(defaultValue = "test-bucket") String bucket) {

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new IllegalArgumentException("셀러를 먼저 생성하세요: /test/data/create-seller"));

        // HLS 마스터 에셋 생성
        MediaAsset hlsAsset = MediaAsset.builder()
                .owner(seller)
                .assetType(AssetType.VIDEO_HLS_MASTER)
                .bucket(bucket)
                .objectKey(hlsObjectKey)
                .contentType("application/vnd.apple.mpegurl")
                .visibility(Visibility.PUBLIC)
                .sizeBytes(1024L)
                .durationMs((long) durationMinutes * 60 * 1000)
                .build();
        hlsAsset = mediaAssetRepository.save(hlsAsset);

        // 포스터 에셋 생성
        MediaAsset posterAsset = MediaAsset.builder()
                .owner(seller)
                .assetType(AssetType.POSTER_IMAGE)
                .bucket("test-bucket")
                .objectKey("test/poster.jpg")
                .contentType("image/jpeg")
                .visibility(Visibility.PUBLIC)
                .sizeBytes(2048L)
                .build();
        posterAsset = mediaAssetRepository.save(posterAsset);

        // 콘텐츠 생성 (ContentStatus: WAITING, PLAYING, ENDING, CLOSED 중 선택)
        // 테스트용으로는 PLAYING 사용 (실제로 재생 가능한 상태)
        Content content = Content.builder()
                .owner(seller)
                .title(title)
                .description("테스트용 콘텐츠입니다.")
                .poster(posterAsset)
                .videoHlsMaster(hlsAsset)
                .status(ContentStatus.PUBLISHED)
                .totalView(0L)
                .monthView(0L)
                .build();
        content = contentRepository.save(content);

        // 스케줄 생성 (현재 시간 기준 PLAYING 상태)
        LocalDate today = LocalDate.now();
        LocalDateTime startAt = LocalDateTime.now().minusMinutes(5); // 5분 전 시작
        LocalDateTime endAt = startAt.plusMinutes(durationMinutes);

        ScheduleDay scheduleDay = ScheduleDay.builder()
                .content(content)
                .scheduleDate(today)
                .isLocked(true) // 편성 확정
                .build();
        scheduleDay = scheduleDayRepository.save(scheduleDay);

        ScheduleItem scheduleItem = ScheduleItem.builder()
                .scheduleDay(scheduleDay)
                .content(content)
                .startAt(startAt)
                .endAt(endAt)
                .status(ScheduleStatus.PLAYING) // 상영 중!
                .build();
        scheduleItem = scheduleItemRepository.save(scheduleItem);

        Map<String, Object> result = new HashMap<>();
        result.put("contentId", content.getContentId());
        result.put("contentTitle", content.getTitle());
        result.put("scheduleItemId", scheduleItem.getScheduleItemId());
        result.put("scheduleDayId", scheduleDay.getScheduleDayId());
        result.put("status", scheduleItem.getStatus().name());
        result.put("startAt", scheduleItem.getStartAt().toString());
        result.put("endAt", scheduleItem.getEndAt().toString());
        result.put("hlsAssetId", hlsAsset.getAssetId());

        return ResponseEntity.ok(ApiResponse.success("PLAYING 상태 스케줄 생성 완료", result));
    }

    /**
     * 테스트용 콘텐츠 + 스케줄 생성 (WAITING 상태 - 10분 후 시작)
     */
    @GetMapping("/create-waiting-schedule")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createWaitingSchedule(
            @RequestParam(defaultValue = "seller@test.com") String sellerEmail,
            @RequestParam(defaultValue = "대기 중인 영화") String title,
            @RequestParam(defaultValue = "120") int durationMinutes,
            @RequestParam(defaultValue = "10") int startsInMinutes) {

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new IllegalArgumentException("셀러를 먼저 생성하세요"));

        MediaAsset hlsAsset = MediaAsset.builder()
                .owner(seller)
                .assetType(AssetType.VIDEO_HLS_MASTER)
                .bucket("test-bucket")
                .objectKey("test/hls/waiting-master.m3u8")
                .contentType("application/vnd.apple.mpegurl")
                .visibility(Visibility.PUBLIC)
                .sizeBytes(1024L)
                .durationMs((long) durationMinutes * 60 * 1000)
                .build();
        hlsAsset = mediaAssetRepository.save(hlsAsset);

        Content content = Content.builder()
                .owner(seller)
                .title(title)
                .description("WAITING 상태 테스트용 콘텐츠")
                .videoHlsMaster(hlsAsset)
                .status(ContentStatus.DRAFT)
                .totalView(0L)
                .monthView(0L)
                .build();
        content = contentRepository.save(content);

        LocalDate today = LocalDate.now();
        LocalDateTime startAt = LocalDateTime.now().plusMinutes(startsInMinutes);
        LocalDateTime endAt = startAt.plusMinutes(durationMinutes);

        ScheduleDay scheduleDay = ScheduleDay.builder()
                .content(content)
                .scheduleDate(today)
                .isLocked(true)
                .build();
        scheduleDay = scheduleDayRepository.save(scheduleDay);

        ScheduleItem scheduleItem = ScheduleItem.builder()
                .scheduleDay(scheduleDay)
                .content(content)
                .startAt(startAt)
                .endAt(endAt)
                .status(ScheduleStatus.WAITING) // 대기 중
                .build();
        scheduleItem = scheduleItemRepository.save(scheduleItem);

        Map<String, Object> result = new HashMap<>();
        result.put("contentId", content.getContentId());
        result.put("scheduleItemId", scheduleItem.getScheduleItemId());
        result.put("status", scheduleItem.getStatus().name());
        result.put("startAt", scheduleItem.getStartAt().toString());
        result.put("startsInMinutes", startsInMinutes);

        return ResponseEntity.ok(ApiResponse.success("WAITING 상태 스케줄 생성 완료", result));
    }

    /**
     * 전체 테스트 데이터 한번에 생성
     * GET /test/data/setup-all
     */
    @GetMapping("/setup-all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setupAll() {
        // 1. 셀러 생성
        createSeller("seller@test.com", "테스트셀러", "12345678");
        
        // 2. 구독자 생성
        createSubscriber("subscriber@test.com", "테스트구독자", "12345678");
        
        // 3. PLAYING 스케줄 생성
        Map<String, Object> result = new HashMap<>();
        result.put("seller", Map.of("email", "seller@test.com", "password", "12345678"));
        result.put("subscriber", Map.of("email", "subscriber@test.com", "password", "12345678"));
        result.put("message", "모든 테스트 데이터가 생성되었습니다. /auth/login으로 로그인하세요.");

        return ResponseEntity.ok(ApiResponse.success("전체 테스트 데이터 생성 완료", result));
    }
}
