package com.tiagoagueda.api.auth.dto;

public record AuthenticationRequest(
        String email,
        String password
) {}