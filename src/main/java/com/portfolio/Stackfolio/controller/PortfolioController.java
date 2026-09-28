package com.portfolio.Stackfolio.controller;

import com.portfolio.Stackfolio.dto.portfolio.PortfolioPublicResponse;
import com.portfolio.Stackfolio.dto.portfolio.PortfolioRequest;
import com.portfolio.Stackfolio.dto.portfolio.PortfolioResponse;
import com.portfolio.Stackfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios")
@Tag(name = "Portfolios", description = "Create, update, and view public portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get a public portfolio by slug")
    public ResponseEntity<PortfolioPublicResponse> getPortfolioBySlug(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(portfolioService.fetchPortfolio(slug));
    }

    @PostMapping
    @Operation(
            summary = "Create the authenticated user's portfolio",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @RequestBody @Valid PortfolioRequest request,
            @Parameter(hidden = true)
            Authentication authentication) {

        JwtAuthenticationToken jwtAuth =
                (JwtAuthenticationToken) authentication;

        Jwt jwt = jwtAuth.getToken();

        Long userId = jwt.getClaim("userId");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(portfolioService.createPortfolio(request, userId));
    }
    @PutMapping
    @Operation(
            summary = "Update the authenticated user's portfolio",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PortfolioResponse> UpdatePortfolio(
            @RequestBody @Valid PortfolioRequest request,
            @Parameter(hidden = true)
            Authentication authentication){
        JwtAuthenticationToken jwtAuth =
                (JwtAuthenticationToken) authentication;

        Jwt jwt = jwtAuth.getToken();

        Long userId = jwt.getClaim("userId");

        return ResponseEntity.ok(portfolioService.updatePortfolio(request, userId));
    }
}
