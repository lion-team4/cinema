package com.example.cinema.config.webSocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.*;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;
    private final StompErrorHandler stompErrorHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 순수 WebSocket (프론트엔드 SockJS 미사용 시)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:3000/",
                        "http://127.0.0.1:3000/",
                        "http://43.200.191.126:3000/",
                        "https://43.200.191.126:3000/"
                );

        // SockJS fallback (Postman, 브라우저 호환성)
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns(
                        "http://localhost:3000/",
                        "http://127.0.0.1:3000/",
                        "http://43.200.191.126:3000/",
                        "https://43.200.191.126:3000/"
                )
                .withSockJS();

        // 에러 핸들러 등록
        registry.setErrorHandler(stompErrorHandler);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}