package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;

public class SmsServiceImpl implements SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @Override
    public void sendOtpSms(String phoneNumber, String otp) {
        try {
            Twilio.init(accountSid, authToken);

            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    "Mã xác thực OTP của bạn là: " + otp + ". Mã này sẽ hết hạn sau 5 phút."
            ).create();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi gửi SMS: " + e.getMessage());
        }
    }
}
