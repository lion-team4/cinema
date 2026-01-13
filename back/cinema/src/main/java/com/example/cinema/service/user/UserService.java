package com.example.cinema.service.user;

import com.example.cinema.config.common.JwtTokenProvider;
import com.example.cinema.dto.auth.LoginRequest;
import com.example.cinema.dto.auth.SignupRequest;
import com.example.cinema.dto.auth.TokenResponse;
import com.example.cinema.dto.user.UserGetResponse;
import com.example.cinema.dto.user.UserUpdateRequest;
import com.example.cinema.dto.user.UserUpdateResponse;
import com.example.cinema.entity.MediaAsset;
import com.example.cinema.entity.User;
import com.example.cinema.repository.MediaAssetRepository;
import com.example.cinema.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.example.cinema.repository.auth.RefreshTokenRepository refreshTokenRepository;


    public UserGetResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return UserGetResponse.from(user);
    }

    @Transactional
    public UserUpdateResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new RuntimeException("이미 존재하는 닉네임입니다.");
            }
        }

        MediaAsset profileImage = null;
        if (request.getProfileImageAssetId() != null) {
            profileImage = mediaAssetRepository.findById(request.getProfileImageAssetId())
                    .orElseThrow(() -> new RuntimeException("프로필 이미지를 찾을 수 없습니다."));
        }

        user.updateProfile(request.getNickname(), profileImage);

        return UserUpdateResponse.from(user);
    }

    @Transactional
    public UserGetResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .seller(false) // Default to false
                .build();

        return UserGetResponse.from(userRepository.save(user));
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenRepository.save(com.example.cinema.entity.RefreshToken.builder()
                .key(authentication.getName())
                .value(refreshToken)
                .build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    public TokenResponse reissue(com.example.cinema.dto.auth.TokenRefreshRequest request) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        // 2. Access Token 에서 User ID 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(request.getAccessToken());

        // 3. 저장소에서 User ID 를 기반으로 Refresh Token 값 가져옴
        com.example.cinema.entity.RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치하는지 검사
        if (!refreshToken.getValue().equals(request.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.generateToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // 6. 저장소 정보 업데이트
        refreshToken.updateValue(newRefreshToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    public void logout(String username) {
        refreshTokenRepository.deleteById(username);
    }

    @Transactional
    public void deleteUser(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        user.withdraw();
    }
}
