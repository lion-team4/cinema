package com.example.cinema.config.webSocket;

import com.example.cinema.config.common.JwtTokenProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.example.cinema.config.common.JwtTokenProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import static com.example.cinema.config.webSocket.JwtTokens.extractBearer;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = firstNativeHeader(accessor, "Authorization");
            String token = extractBearer(auth);

            if (token == null || !jwtTokenProvider.validateToken(token)) {
                throw new MessagingException("Unauthorized: invalid or missing token");
            }

            accessor.setUser(jwtTokenProvider.getAuthentication(token));
        }

        return message;
    }

    private static String firstNativeHeader(StompHeaderAccessor accessor, String key) {
        List<String> values = accessor.getNativeHeader(key);
        if (values == null || values.isEmpty()) return null;
        return values.get(0);
    }

    private static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        String v = authorizationHeader.trim();
        if (!v.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
        String token = v.substring(7).trim();
        return token.isBlank() ? null : token;
    }
}