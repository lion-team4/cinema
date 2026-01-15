package com.example.cinema.entity;

import com.example.cinema.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_asset_id")
    private MediaAsset profileImage;

    //내 구독 정보
    @OneToOne(fetch = FetchType.LAZY)
    private Subscription subscription;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean seller = false;

    public void withdraw() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updateProfile(String nickname, MediaAsset profileImage) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }
    public String getCustomerKey(){
        return "CUSTOMER_" + this.userId;
    }

    // 구독한 유저 Seller 로 변경
    public void promoteToSeller() {
        this.seller = true;
    }

    // 필요하다면 구독 해지 시 일반 유저로 변경
    public void demoteFromSeller() {
        this.seller = false;
    }
}
