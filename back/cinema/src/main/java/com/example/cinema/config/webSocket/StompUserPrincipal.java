package com.example.cinema.config.webSocket;

import java.security.Principal;

public final class StompUserPrincipal implements Principal {

    public static final String ATTR_KEY = "STOMP_PRINCIPAL";

    private final String name;

    public StompUserPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}

