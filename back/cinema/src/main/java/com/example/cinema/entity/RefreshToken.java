package com.example.cinema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "refresh_token")
public class -RefreshToken {

    @Id
    @Column(name = "rt_key")
    private String key; // 사용자 ID (email 등 식별자)

    @Column(name = "rt_value")
    private String value; // Refresh Token 값

    public RefreshToken updateValue(String token) {
        this.value = token;
        return this;
    }
}
