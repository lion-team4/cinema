package com.example.cinema.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {
    
    @Size(min = 2, max = 20)
    private String nickname;
    
    private Long profileImageAssetId;
}
