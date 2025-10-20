package com.example.SocialMedia.service;

import com.example.SocialMedia.constant.OtpChannel;
import com.example.SocialMedia.model.coredata_model.User;

public interface OtpService {
    String generateOtp();
    void sendOtp(String identifier, User user, OtpChannel channel);

    boolean verifyOtp(String identifier, String otp, OtpChannel channel);
    long getOtpExpiryTime(String identifier);
}
