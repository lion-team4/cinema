package com.example.cinema.config.webSocket;

import com.example.cinema.config.common.JwtTokenProvider;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
@RequiredArgsConstructor
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = JwtTokens.extractBearer(auth);

        if (token == null) return false;
        if (!jwtTokenProvider.validateToken(token)) return false;

        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // STOMP에서 Principal로 쓰기 위해 attributes에 저장
        // (HandshakeHandler가 이 값을 Principal로 꺼내 쓰도록 구성되어 있어야 함)
        Principal principal = new StompUserPrincipal(authentication.getName());
        attributes.put(StompUserPrincipal.ATTR_KEY, principal);

        // 필요하면 Authentication도 같이 저장해둘 수 있음
        attributes.put("AUTHENTICATION", authentication);

        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }
}