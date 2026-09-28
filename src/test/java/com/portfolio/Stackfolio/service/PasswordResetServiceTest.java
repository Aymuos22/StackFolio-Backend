package com.portfolio.Stackfolio.service;

import com.portfolio.Stackfolio.dto.auth.ForgotPasswordRequest;
import com.portfolio.Stackfolio.dto.auth.MessageResponse;
import com.portfolio.Stackfolio.dto.auth.ResetPasswordRequest;
import com.portfolio.Stackfolio.entity.User;
import com.portfolio.Stackfolio.exception.BadRequestException;
import com.portfolio.Stackfolio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordResetServiceTest {

    private static final String GENERIC_FORGOT_PASSWORD_MESSAGE =
            "If that email exists, an OTP has been sent";

    private FakeUserRepository fakeUserRepository;
    private FakePasswordEncoder passwordEncoder;
    private FakeOtpService otpService;
    private FakeEmailService emailService;
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        fakeUserRepository = new FakeUserRepository();
        passwordEncoder = new FakePasswordEncoder();
        otpService = new FakeOtpService();
        emailService = new FakeEmailService();
        passwordResetService = new PasswordResetService(
                fakeUserRepository.proxy(),
                passwordEncoder,
                otpService,
                emailService
        );
    }

    @Test
    void forgotPasswordSendsOtpWhenUserExists() {
        fakeUserRepository.userByEmail = Optional.of(new User());
        otpService.createdOtp = "123456";

        MessageResponse response = passwordResetService.forgotPassword(
                forgotPasswordRequest(" User@Example.COM ")
        );

        assertEquals(GENERIC_FORGOT_PASSWORD_MESSAGE, response.getMessage());
        assertEquals("user@example.com", fakeUserRepository.lastFindByEmail);
        assertEquals("user@example.com", otpService.lastCreateOtpEmail);
        assertEquals("user@example.com", emailService.toEmail);
        assertEquals("123456", emailService.otp);
    }

    @Test
    void forgotPasswordDoesNotRevealMissingUser() {
        fakeUserRepository.userByEmail = Optional.empty();

        MessageResponse response = passwordResetService.forgotPassword(
                forgotPasswordRequest("missing@example.com")
        );

        assertEquals(GENERIC_FORGOT_PASSWORD_MESSAGE, response.getMessage());
        assertEquals("missing@example.com", fakeUserRepository.lastFindByEmail);
        assertEquals(0, otpService.cooldownChecks);
        assertNull(otpService.lastCreateOtpEmail);
        assertNull(emailService.toEmail);
    }

    @Test
    void forgotPasswordSkipsOtpCreationDuringCooldown() {
        fakeUserRepository.userByEmail = Optional.of(new User());
        otpService.inCooldown = true;

        MessageResponse response = passwordResetService.forgotPassword(
                forgotPasswordRequest("user@example.com")
        );

        assertEquals(GENERIC_FORGOT_PASSWORD_MESSAGE, response.getMessage());
        assertEquals(1, fakeUserRepository.findByEmailCalls);
        assertEquals(1, otpService.cooldownChecks);
        assertNull(otpService.lastCreateOtpEmail);
        assertNull(emailService.toEmail);
    }

    @Test
    void forgotPasswordReturnsGenericWhenRedisFails() {
        fakeUserRepository.userByEmail = Optional.of(new User());
        otpService.cooldownExceptionToThrow =
                new IllegalStateException("redis unavailable");

        MessageResponse response = passwordResetService.forgotPassword(
                forgotPasswordRequest("user@example.com")
        );

        assertEquals(GENERIC_FORGOT_PASSWORD_MESSAGE, response.getMessage());
        assertNull(otpService.lastCreateOtpEmail);
        assertNull(emailService.toEmail);
    }

    @Test
    void forgotPasswordClearsOtpStateWhenEmailProviderFails() {
        fakeUserRepository.userByEmail = Optional.of(new User());
        otpService.createdOtp = "123456";
        emailService.exceptionToThrow = new IllegalStateException("provider down");

        MessageResponse response = passwordResetService.forgotPassword(
                forgotPasswordRequest("user@example.com")
        );

        assertEquals(GENERIC_FORGOT_PASSWORD_MESSAGE, response.getMessage());
        assertEquals("user@example.com", otpService.lastClearedEmail);
    }

    @Test
    void resetPasswordUpdatesPasswordWhenOtpIsValid() {
        User user = new User();
        fakeUserRepository.userByEmail = Optional.of(user);
        otpService.otpValid = true;
        passwordEncoder.encodedPassword = "encoded-password";

        MessageResponse response = passwordResetService.resetPassword(
                resetPasswordRequest(" USER@example.com ", "123456", "newPassword1")
        );

        assertEquals("Password reset successfully", response.getMessage());
        assertEquals("encoded-password", user.getPasswordHash());
        assertEquals("newPassword1", passwordEncoder.lastRawPassword);
        assertEquals(user, fakeUserRepository.savedUser);
        assertEquals("user@example.com", otpService.lastClearedEmail);
    }

    @Test
    void resetPasswordRejectsInvalidOtp() {
        User user = new User();
        fakeUserRepository.userByEmail = Optional.of(user);
        otpService.otpValid = false;

        assertThrows(
                BadRequestException.class,
                () -> passwordResetService.resetPassword(
                        resetPasswordRequest(
                                "user@example.com",
                                "000000",
                                "newPassword1"
                        )
                )
        );

        assertNull(fakeUserRepository.savedUser);
        assertNull(otpService.lastClearedEmail);
    }

    private ForgotPasswordRequest forgotPasswordRequest(String email) {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(email);
        return request;
    }

    private ResetPasswordRequest resetPasswordRequest(
            String email,
            String otp,
            String newPassword
    ) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(email);
        request.setOtp(otp);
        request.setNewPassword(newPassword);
        return request;
    }

    private static final class FakeUserRepository {
        private Optional<User> userByEmail = Optional.empty();
        private String lastFindByEmail;
        private int findByEmailCalls;
        private User savedUser;

        private UserRepository proxy() {
            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[] {UserRepository.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("findByEmail")) {
                            findByEmailCalls++;
                            lastFindByEmail = (String) args[0];
                            return userByEmail;
                        }
                        if (method.getName().equals("save")) {
                            savedUser = (User) args[0];
                            return savedUser;
                        }
                        if (method.getName().equals("toString")) {
                            return "FakeUserRepository";
                        }
                        if (method.getName().equals("hashCode")) {
                            return System.identityHashCode(proxy);
                        }
                        if (method.getName().equals("equals")) {
                            return proxy == args[0];
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static final class FakePasswordEncoder implements PasswordEncoder {
        private String encodedPassword = "encoded";
        private String lastRawPassword;

        @Override
        public String encode(CharSequence rawPassword) {
            lastRawPassword = rawPassword.toString();
            return encodedPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return false;
        }
    }

    private static final class FakeOtpService extends OtpService {
        private boolean inCooldown;
        private String createdOtp = "123456";
        private boolean otpValid;
        private int cooldownChecks;
        private RuntimeException cooldownExceptionToThrow;
        private String lastCreateOtpEmail;
        private String lastVerifyEmail;
        private String lastVerifyOtp;
        private String lastClearedEmail;

        private FakeOtpService() {
            super(null, null, 300, 60, 5);
        }

        @Override
        public boolean isInCooldown(String email) {
            cooldownChecks++;
            if (cooldownExceptionToThrow != null) {
                throw cooldownExceptionToThrow;
            }
            return inCooldown;
        }

        @Override
        public String createOtp(String email) {
            lastCreateOtpEmail = email;
            return createdOtp;
        }

        @Override
        public boolean verifyOtp(String email, String otp) {
            lastVerifyEmail = email;
            lastVerifyOtp = otp;
            return otpValid;
        }

        @Override
        public void clearPasswordResetState(String email) {
            lastClearedEmail = email;
        }
    }

    private static final class FakeEmailService extends EmailService {
        private String toEmail;
        private String otp;
        private RuntimeException exceptionToThrow;

        private FakeEmailService() {
            super("test-key", "test@example.com", 300);
        }

        @Override
        public void sendPasswordResetOtp(String toEmail, String otp) {
            this.toEmail = toEmail;
            this.otp = otp;
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
        }
    }
}
