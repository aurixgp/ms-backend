package com.boutique.security;

import com.boutique.common.AppProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record LoginResponse(String token, String email, String fullName) {}
    public record Me(String email, String fullName) {}

    private final AdminUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwt;
    private final AppProperties props;

    public AuthController(AdminUserRepository users, PasswordEncoder encoder, JwtEncoder jwt, AppProperties props) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.props = props;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        AdminUser user = users.findByEmailIgnoreCase(req.email())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects"));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("boutique-api")
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(props.security().tokenHours())))
                .claim("name", user.getFullName())
                .claim("roles", List.of("ADMIN"))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwt.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponse(token, user.getEmail(), user.getFullName());
    }

    @GetMapping("/me")
    public Me me(@AuthenticationPrincipal Jwt principal) {
        return new Me(principal.getSubject(), principal.getClaimAsString("name"));
    }
}
