package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.constant.OtpChannel;
import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.service.EmailService;
import com.example.SocialMedia.service.OtpService;
import com.example.SocialMedia.service.SmsService;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;

public class OtpServiceImpl implements OtpService {
    private final StringRedisTemplate stringRedisTemplate;
    private final EmailService emailService;
    private final SmsService smsService;

    public OtpServiceImpl(StringRedisTemplate stringRedisTemplate, EmailService emailService, SmsService smsService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.emailService = emailService;
        this.smsService = smsService;
    }
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_STATUS_PREFIX = "otp_status:";
    private static final String OTP_ATTEMPT_PREFIX = "otp:attempt:";
    static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final int BLOCK_TIME_MINUTES = 15;

    @Override
    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    @Override
    public void sendOtp(String identifier, User user, OtpChannel channel) {
        String blockKey = OTP_STATUS_PREFIX + identifier;
        String status = stringRedisTemplate.opsForValue().get(blockKey);
        if ("BLOCKED".equals(status)) {
            throw new RuntimeException("Tài khoản bị khóa tạm thời. Vui lòng thử lại sau");
        }
        String otp = generateOtp();
        String otpKey = OTP_KEY_PREFIX + identifier;
        stringRedisTemplate.opsForValue().set(otpKey, otp, Duration.ofMinutes(OTP_EXPIRY_MINUTES));

        if(channel.equals(OtpChannel.EMAIL)) {
            emailService.sendEmail(identifier, otp);
        } else if(channel.equals(OtpChannel.SMS)) {
            smsService.sendOtpSms(identifier, otp);
        }
        stringRedisTemplate.delete(OTP_ATTEMPT_PREFIX + identifier);
    }
    @Override
    public boolean verifyOtp(String identifier, String otp, OtpChannel otpChannel) {
        String otpKey = OTP_KEY_PREFIX + identifier;
        String storedOtp = stringRedisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            throw new RuntimeException("OTP đã hết hạn hoặc không tồn tại");
        }

        if (!storedOtp.equals(otp)) {
            String attemptKey = OTP_ATTEMPT_PREFIX + identifier;
            long attempts = Objects.requireNonNull(
                    stringRedisTemplate.opsForValue().increment(attemptKey),
                    "Lỗi Redis: Không thể lấy số lần thử."
            );

            if (attempts == 1) {
                stringRedisTemplate.expire(attemptKey, Duration.ofMinutes(BLOCK_TIME_MINUTES));
            }

            if (attempts >= MAX_ATTEMPTS) {
                String blockKey = OTP_STATUS_PREFIX + identifier;
                stringRedisTemplate.opsForValue()
                        .set(blockKey, "BLOCKED", Duration.ofMinutes(BLOCK_TIME_MINUTES));
                stringRedisTemplate.delete(otpKey);

                throw new RuntimeException("Sai OTP quá nhiều lần. Tài khoản bị khóa 15 phút");
            }

            throw new RuntimeException("OTP không chính xác. Còn " +
                    (MAX_ATTEMPTS - attempts) + " lần thử");
        }

        stringRedisTemplate.delete(otpKey);
        stringRedisTemplate.delete(OTP_ATTEMPT_PREFIX + identifier);
        stringRedisTemplate.delete(OTP_STATUS_PREFIX + identifier);

        return true;
    }

    public long getOtpExpiryTime(String identifier) {
        String otpKey = OTP_KEY_PREFIX + identifier;
        Long expiry = stringRedisTemplate.getExpire(otpKey, java.util.concurrent.TimeUnit.SECONDS);
        return expiry != null ? expiry : -1;
    }
}
