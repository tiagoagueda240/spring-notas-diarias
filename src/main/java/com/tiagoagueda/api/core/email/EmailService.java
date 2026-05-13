package com.tiagoagueda.api.core.email;

public interface EmailService {
    void sendPasswordResetEmail(String to, String resetLink);
}
