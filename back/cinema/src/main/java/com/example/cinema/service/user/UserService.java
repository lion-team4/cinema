package com.example.cinema.service.user;

import com.example.cinema.config.auth.JwtTokenProvider;
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

    public TokenResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        String jwt = jwtTokenProvider.generateToken(authentication);

        return TokenResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .build();
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
