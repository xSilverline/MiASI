package com.mission.accesscontrol;

public class Identity {
    private final String login;
    private final String passwordHash;

    public Identity(String login, String passwordHash) {
        this.login = login;
        this.passwordHash = passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}