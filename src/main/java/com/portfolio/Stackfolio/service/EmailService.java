package com.portfolio.Stackfolio.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EmailService {

    private final String apiKey;
    private final String fromEmail;
    private final Duration otpTtl;

    public EmailService(
            @Value("${stackfolio.email.resend.api-key:}") String apiKey,
            @Value("${stackfolio.email.from:}") String fromEmail,
            @Value("${stackfolio.password-reset.otp-ttl-seconds:300}") long otpTtlSeconds
    ) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.otpTtl = Duration.ofSeconds(otpTtlSeconds);
    }

    public void sendPasswordResetOtp(String toEmail, String otp) {
        if (apiKey == null || apiKey.isBlank() || fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("Email provider is not configured");
        }

        Resend resend = new Resend(apiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Your Stackfolio password reset code")
                .html(passwordResetHtml(otp))
                .text(passwordResetText(otp))
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException exception) {
            throw new IllegalStateException("Unable to send password reset email", exception);
        }
    }

    private String passwordResetText(String otp) {
        return """
                Your Stackfolio password reset code is %s.

                It expires in %s.
                If you did not request this, you can ignore this email.
                """.formatted(otp, expiryText());
    }

    private String passwordResetHtml(String otp) {
        return """
                <p>Your Stackfolio password reset code is:</p>
                <h2>%s</h2>
                <p>This code expires in %s.</p>
                <p>If you did not request this, you can ignore this email.</p>
                """.formatted(otp, expiryText());
    }

    private String expiryText() {
        long seconds = otpTtl.toSeconds();
        if (seconds % 60 == 0) {
            long minutes = seconds / 60;
            return minutes == 1 ? "1 minute" : minutes + " minutes";
        }
        return seconds == 1 ? "1 second" : seconds + " seconds";
    }
}
