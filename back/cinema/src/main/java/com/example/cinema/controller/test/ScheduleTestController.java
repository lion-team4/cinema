package com.example.cinema.controller.test;

import com.example.cinema.dto.common.ApiResponse;
import com.example.cinema.entity.*;
import com.example.cinema.repository.content.ContentRepository;
import com.example.cinema.repository.mediaAsset.MediaAssetRepository;
import com.example.cinema.repository.schedule.ScheduleDayRepository;
import com.example.cinema.repository.schedule.ScheduleItemRepository;
import com.example.cinema.repository.user.UserRepository;
import com.example.cinema.type.AssetType;
import com.example.cinema.type.ContentStatus;
import com.example.cinema.type.ScheduleStatus;
import com.example.cinema.type.Visibility;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 스케줄 상태 변화 테스트 컨트롤러
 * - CLOSED → WAITING → PLAYING → ENDING → CLOSED 상태 변화 테스트
 * - dev, test 프로필에서만 활성화
 */
@Controller
@RequestMapping("/test/schedule")
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class ScheduleTestController {

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final ScheduleItemRepository scheduleItemRepository;

    /**
     * 스케줄 테스트 메인 페이지
     */
    @GetMapping
    public String schedulePage(Model model) {
        // 모든 스케줄 아이템 조회
        List<ScheduleItem> schedules = scheduleItemRepository.findAll();
        model.addAttribute("schedules", schedules);
        return "test/schedule-test";
    }

    /**
     * 테스트용 스케줄 생성 API
     * @param minutesFromNow 현재로부터 몇 분 후에 시작할지 (음수면 과거)
     * @param durationMinutes 상영 시간 (분)
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSchedule(
            @RequestParam(defaultValue = "seller@test.com") String sellerEmail,
            @RequestParam(defaultValue = "1") int minutesFromNow,
            @RequestParam(defaultValue = "5") int durationMinutes,
            @RequestParam(defaultValue = "테스트 영화") String title) {

        User seller = userRepository.findByEmail(sellerEmail).orElse(null);
        if (seller == null) {
            // 셀러가 없으면 생성
            seller = userRepository.save(User.builder()
                    .email(sellerEmail)
                    .nickname("테스트셀러")
                    .passwordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGf6r/M2VZBbCT8P1vQZ3h1oUZ6S")
                    .seller(true)
                    .build());
        }

        // HLS 에셋 생성
        MediaAsset hlsAsset = mediaAssetRepository.save(MediaAsset.builder()
                .owner(seller)
                .assetType(AssetType.VIDEO_HLS_MASTER)
                .bucket("test-bucket")
                .objectKey("test/hls/master.m3u8")
                .contentType("application/vnd.apple.mpegurl")
                .visibility(Visibility.PUBLIC)
                .sizeBytes(1024L)
                .durationMs((long) durationMinutes * 60 * 1000)
                .build());

        // 콘텐츠 생성
        Content content = contentRepository.save(Content.builder()
                .owner(seller)
                .title(title + " (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")")
                .description("스케줄 상태 테스트용 콘텐츠")
                .videoHlsMaster(hlsAsset)
                .status(ContentStatus.PUBLISHED)
                .totalView(0L)
                .monthView(0L)
                .build());

        // 스케줄 생성
        LocalDateTime startAt = LocalDateTime.now().plusMinutes(minutesFromNow);
        LocalDateTime endAt = startAt.plusMinutes(durationMinutes);

        ScheduleDay scheduleDay = scheduleDayRepository.save(ScheduleDay.builder()
                .content(content)
                .scheduleDate(LocalDate.now())
                .isLocked(true)
                .build());

        // 초기 상태 결정 (스케줄러 로직 기반)
        ScheduleStatus initialStatus = determineInitialStatus(startAt, endAt);

        ScheduleItem scheduleItem = scheduleItemRepository.save(ScheduleItem.builder()
                .scheduleDay(scheduleDay)
                .content(content)
                .startAt(startAt)
                .endAt(endAt)
                .status(initialStatus)
                .build());

        Map<String, Object> result = new HashMap<>();
        result.put("scheduleItemId", scheduleItem.getScheduleItemId());
        result.put("contentTitle", content.getTitle());
        result.put("status", scheduleItem.getStatus().name());
        result.put("startAt", startAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("endAt", endAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("message", getStatusDescription(initialStatus, startAt));

        return ResponseEntity.ok(ApiResponse.success("스케줄 생성 완료", result));
    }

    /**
     * 스케줄 상태 조회 API
     */
    @GetMapping("/status/{scheduleItemId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> getScheduleStatus(
            @PathVariable Long scheduleItemId) {

        ScheduleItem item = scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));

        Map<String, Object> result = new HashMap<>();
        result.put("scheduleItemId", item.getScheduleItemId());
        result.put("contentTitle", item.getContent().getTitle());
        result.put("status", item.getStatus().name());
        result.put("startAt", item.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("endAt", item.getEndAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 다음 상태 변화까지 남은 시간 계산
        result.put("nextTransition", calculateNextTransition(item));

        return ResponseEntity.ok(ApiResponse.success("스케줄 상태 조회 성공", result));
    }

    /**
     * 모든 스케줄 상태 조회 API
     */
    @GetMapping("/status/all")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllScheduleStatus() {
        List<ScheduleItem> items = scheduleItemRepository.findAll();

        List<Map<String, Object>> result = items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("scheduleItemId", item.getScheduleItemId());
            map.put("contentTitle", item.getContent().getTitle());
            map.put("status", item.getStatus().name());
            map.put("startAt", item.getStartAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            map.put("endAt", item.getEndAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("전체 스케줄 조회 성공", result));
    }

    /**
     * 스케줄 삭제 API
     */
    @DeleteMapping("/{scheduleItemId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long scheduleItemId) {
        scheduleItemRepository.deleteById(scheduleItemId);
        return ResponseEntity.ok(ApiResponse.success("스케줄 삭제 완료"));
    }

    /**
     * 모든 테스트 스케줄 삭제
     */
    @DeleteMapping("/all")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> deleteAllSchedules() {
        scheduleItemRepository.deleteAll();
        scheduleDayRepository.deleteAll();
        return ResponseEntity.ok(ApiResponse.success("모든 스케줄 삭제 완료"));
    }

    // --- Helper Methods ---

    private ScheduleStatus determineInitialStatus(LocalDateTime startAt, LocalDateTime endAt) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(startAt.minusMinutes(10))) {
            return ScheduleStatus.CLOSED;  // 시작 10분 전보다 이전
        } else if (now.isBefore(startAt)) {
            return ScheduleStatus.WAITING; // 시작 10분 전 ~ 시작
        } else if (now.isBefore(endAt)) {
            return ScheduleStatus.PLAYING; // 시작 ~ 종료
        } else if (now.isBefore(endAt.plusMinutes(10))) {
            return ScheduleStatus.ENDING;  // 종료 ~ 종료+10분
        } else {
            return ScheduleStatus.CLOSED;  // 종료+10분 이후
        }
    }

    private String getStatusDescription(ScheduleStatus status, LocalDateTime startAt) {
        return switch (status) {
            case CLOSED -> "CLOSED 상태. 시작 10분 전(" + startAt.minusMinutes(10).format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")에 WAITING으로 변경됩니다.";
            case WAITING -> "WAITING 상태. 시작 시각(" + startAt.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")에 PLAYING으로 변경됩니다.";
            case PLAYING -> "PLAYING 상태 (상영 중)";
            case ENDING -> "ENDING 상태 (종료 중)";
        };
    }

    private String calculateNextTransition(ScheduleItem item) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAt = item.getStartAt();
        LocalDateTime endAt = item.getEndAt();

        return switch (item.getStatus()) {
            case CLOSED -> {
                if (now.isBefore(startAt.minusMinutes(10))) {
                    long seconds = java.time.Duration.between(now, startAt.minusMinutes(10)).getSeconds();
                    yield "→ WAITING까지 " + formatDuration(seconds);
                }
                yield "상영 종료됨";
            }
            case WAITING -> {
                long seconds = java.time.Duration.between(now, startAt).getSeconds();
                yield "→ PLAYING까지 " + formatDuration(seconds);
            }
            case PLAYING -> {
                long seconds = java.time.Duration.between(now, endAt).getSeconds();
                yield "→ ENDING까지 " + formatDuration(seconds);
            }
            case ENDING -> {
                long seconds = java.time.Duration.between(now, endAt.plusMinutes(10)).getSeconds();
                yield "→ CLOSED까지 " + formatDuration(seconds);
            }
        };
    }

    private String formatDuration(long seconds) {
        if (seconds < 0) return "곧 변경됨";
        long minutes = seconds / 60;
        long secs = seconds % 60;
        if (minutes > 0) {
            return minutes + "분 " + secs + "초";
        }
        return secs + "초";
    }
}
