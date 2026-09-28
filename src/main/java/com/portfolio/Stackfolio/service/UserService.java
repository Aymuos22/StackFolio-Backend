package com.portfolio.Stackfolio.service;
import com.portfolio.Stackfolio.dto.auth.SigninRequest;
import com.portfolio.Stackfolio.dto.auth.SigninResponse;
import com.portfolio.Stackfolio.dto.auth.SignupRequest;
import com.portfolio.Stackfolio.dto.auth.SignupResponse;
import com.portfolio.Stackfolio.entity.User;
import com.portfolio.Stackfolio.exception.ConflictException;
import com.portfolio.Stackfolio.exception.UnauthorizedException;
import com.portfolio.Stackfolio.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder=passwordEncoder;
        this.jwtService=jwtService;
    }
    @Transactional
    public SignupResponse SignUp(SignupRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        String encodedPassword= passwordEncoder.encode(request.getPassword());
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getUsername()
        );
        return new SignupResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                token
        );
    }

    public SigninResponse SignIn(SigninRequest request) {

        User user;

        if (request.getUsername() != null && !request.getUsername().isBlank()) {

            user = userRepository.findByUsername(request.getUsername().trim())
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        } else if (request.getEmail() != null && !request.getEmail().isBlank()) {

            user = userRepository.findByEmail(
                            request.getEmail().trim().toLowerCase(Locale.ROOT)
                    )
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        } else {

            throw new UnauthorizedException("Invalid credentials");
        }

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getUsername()
        );

        return new SigninResponse(
                user.getEmail(),
                user.getUsername(),
                true,
                token
        );
    }
}
