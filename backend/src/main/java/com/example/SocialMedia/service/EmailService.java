package com.example.SocialMedia.service;

public interface EmailService {
    void sendEmail(String to, String otp);
    String buildOtpEmailBody(String otp);
}
