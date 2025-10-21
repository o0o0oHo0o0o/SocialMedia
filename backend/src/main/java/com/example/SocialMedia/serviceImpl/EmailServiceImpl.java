package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.example.SocialMedia.serviceImpl.OtpServiceImpl.OTP_EXPIRY_MINUTES;

public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("Otp Verification");
            message.setText(buildOtpEmailBody(otp));

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Email sending failed: " + e.getMessage());
        }
    }

    @Override
    public String buildOtpEmailBody(String otp) {
        try {
            String template = new String(
                    Objects.requireNonNull(getClass().getResourceAsStream("/templates/otp_template.html")).readAllBytes(),
                    StandardCharsets.UTF_8
            );
            template = template.replace("{{OTP}}", otp);
            template = template.replace("{{EXPIRY}}", String.valueOf(OTP_EXPIRY_MINUTES));

            return template;
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc template email", e);
        }
    }


}
