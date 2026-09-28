package com.portfolio.Stackfolio.controller;

import com.portfolio.Stackfolio.dto.auth.ForgotPasswordRequest;
import com.portfolio.Stackfolio.dto.auth.MessageResponse;
import com.portfolio.Stackfolio.dto.auth.ResetPasswordRequest;
import com.portfolio.Stackfolio.dto.auth.SigninRequest;
import com.portfolio.Stackfolio.dto.auth.SigninResponse;
import com.portfolio.Stackfolio.dto.auth.SignupRequest;
import com.portfolio.Stackfolio.dto.auth.SignupResponse;
import com.portfolio.Stackfolio.service.PasswordResetService;
import com.portfolio.Stackfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Signup and signin endpoints")
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            UserService userService,
            PasswordResetService passwordResetService
    ) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Create a new user account")
    public ResponseEntity<SignupResponse> SignUp(
            @RequestBody @Valid SignupRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.SignUp(request));
    }

    @PostMapping("/signin")
    @Operation(summary = "Sign in and receive a JWT")
    public ResponseEntity<SigninResponse> SignIn(
            @RequestBody @Valid SigninRequest request
    ){
        return ResponseEntity.ok(userService.SignIn(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send a password reset OTP if the email exists")
    public ResponseEntity<MessageResponse> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(passwordResetService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using a valid OTP")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(passwordResetService.resetPassword(request));
    }
}
