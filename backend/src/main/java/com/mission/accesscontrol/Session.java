package com.mission.accesscontrol;

import java.util.UUID;

public class Session {
    private final String sessionToken;
    private final String login;

    public Session(String login) {
        this.sessionToken = UUID.randomUUID().toString();
        this.login = login;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}