package com.example.cinema.dto.billing;

import com.example.cinema.entity.BillingKey;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 빌링키 정보 응답 DTO
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Schema(description = "빌링키 정보 응답")
public class BillingResponse {

    @Schema(description = "빌링키 (마스킹 등 보안 처리 권장, 여기서는 원본 반환 가능성 확인 필요)")
    private final String billingKey;

    @Schema(description = "고객 식별 키")
    private final String customerKey;

    @Schema(description = "카드사", example = "SAMSUNG")
    private final String cardCompany;

    @Schema(description = "카드 번호 (일부 마스킹)", example = "1234-5678-****-****")
    private final String cardNumber;

    @Schema(description = "카드 타입", example = "신용 / 체크")
    private final String cardType;

    @Schema(description = "인증 일시")
    private final LocalDateTime authenticatedAt;

    public static BillingResponse from(BillingKey billingKey) {
        return BillingResponse.builder()
                .billingKey(billingKey.getBillingKey())
                .customerKey(billingKey.getCustomerKey())
                .cardCompany(billingKey.getCardCompany())
                .cardNumber(billingKey.getCardNumber())
                .cardType(billingKey.getCardType())
                .authenticatedAt(billingKey.getAuthenticatedAt())
                .build();
    }
}
