package com.portfolio.Stackfolio.service;

import com.portfolio.Stackfolio.dto.auth.ForgotPasswordRequest;
import com.portfolio.Stackfolio.dto.auth.MessageResponse;
import com.portfolio.Stackfolio.dto.auth.ResetPasswordRequest;
import com.portfolio.Stackfolio.entity.User;
import com.portfolio.Stackfolio.exception.BadRequestException;
import com.portfolio.Stackfolio.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PasswordResetService.class);

    private static final String GENERIC_FORGOT_PASSWORD_MESSAGE =
            "If that email exists, an OTP has been sent";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            OtpService otpService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return new MessageResponse(GENERIC_FORGOT_PASSWORD_MESSAGE);
        }

        boolean otpCreated = false;

        try {
            if (otpService.isInCooldown(email)) {
                return new MessageResponse(GENERIC_FORGOT_PASSWORD_MESSAGE);
            }

            String otp = otpService.createOtp(email);
            otpCreated = true;
            emailService.sendPasswordResetOtp(email, otp);
        } catch (RuntimeException exception) {
            if (otpCreated) {
                otpService.clearPasswordResetState(email);
            }

            LOGGER.warn(
                    "Failed to process password reset OTP for {}: {}",
                    maskEmail(email),
                    exception.getMessage()
            );
        }

        return new MessageResponse(GENERIC_FORGOT_PASSWORD_MESSAGE);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException("Invalid or expired OTP")
                );

        boolean validOtp = otpService.verifyOtp(email, request.getOtp());
        if (!validOtp) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpService.clearPasswordResetState(email);

        return new MessageResponse("Password reset successfully");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }

        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
