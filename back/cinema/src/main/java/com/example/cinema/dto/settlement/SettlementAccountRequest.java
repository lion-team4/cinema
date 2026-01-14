package com.example.cinema.dto.settlement;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정산 계좌 등록 요청 DTO
 * <p>
 * 용도:
 * - 크리에이터 정산 계좌 정보 등록/수정 (POST /settlements/accounts)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementAccountRequest {

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    private String accountHolder;
}