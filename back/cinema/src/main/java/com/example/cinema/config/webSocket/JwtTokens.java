package com.example.cinema.config.webSocket;

public final class JwtTokens {

    private JwtTokens() {}

    public static String extractBearer(String authorizationHeader) {
        if (authorizationHeader == null) return null;
        String v = authorizationHeader.trim();
        if (!v.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
        String token = v.substring(7).trim();
        return token.isBlank() ? null : token;
    }
}