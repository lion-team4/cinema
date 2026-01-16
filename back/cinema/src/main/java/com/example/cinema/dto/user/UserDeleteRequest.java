package com.example.cinema.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserDeleteRequest {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
