package com.cinnamon.backend.model;

public record RegistrationRequest(
    String email,
    String password,
    String displayName
) {}
