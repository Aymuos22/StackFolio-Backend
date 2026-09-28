package com.portfolio.Stackfolio.service;

import com.portfolio.Stackfolio.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {

    private static final String OTP_PREFIX = "password-reset:otp:";
    private static final String ATTEMPTS_PREFIX = "password-reset:attempts:";
    private static final String COOLDOWN_PREFIX = "password-reset:cooldown:";

    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Duration otpTtl;
    private final Duration cooldownTtl;
    private final int maxAttempts;

    public OtpService(
            StringRedisTemplate redisTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${stackfolio.password-reset.otp-ttl-seconds:300}") long otpTtlSeconds,
            @Value("${stackfolio.password-reset.cooldown-seconds:60}") long cooldownSeconds,
            @Value("${stackfolio.password-reset.max-attempts:5}") int maxAttempts
    ) {
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.otpTtl = Duration.ofSeconds(otpTtlSeconds);
        this.cooldownTtl = Duration.ofSeconds(cooldownSeconds);
        this.maxAttempts = maxAttempts;
    }

    public boolean isInCooldown(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey(email)));
    }

    public String createOtp(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));

        redisTemplate.opsForValue().set(
                otpKey(email),
                passwordEncoder.encode(otp),
                otpTtl
        );
        redisTemplate.delete(attemptsKey(email));
        redisTemplate.opsForValue().set(cooldownKey(email), "1", cooldownTtl);

        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String attemptsKey = attemptsKey(email);
        String currentAttempts = redisTemplate.opsForValue().get(attemptsKey);

        if (currentAttempts != null
                && Integer.parseInt(currentAttempts) >= maxAttempts) {
            throw new TooManyRequestsException("Too many invalid OTP attempts");
        }

        String otpHash = redisTemplate.opsForValue().get(otpKey(email));
        if (otpHash == null || !passwordEncoder.matches(otp, otpHash)) {
            incrementAttempts(email);
            return false;
        }

        return true;
    }

    public void clearPasswordResetState(String email) {
        redisTemplate.delete(otpKey(email));
        redisTemplate.delete(attemptsKey(email));
        redisTemplate.delete(cooldownKey(email));
    }

    private void incrementAttempts(String email) {
        String key = attemptsKey(email);
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, otpTtl);
        }
    }

    private String otpKey(String email) {
        return OTP_PREFIX + email;
    }

    private String attemptsKey(String email) {
        return ATTEMPTS_PREFIX + email;
    }

    private String cooldownKey(String email) {
        return COOLDOWN_PREFIX + email;
    }
}
