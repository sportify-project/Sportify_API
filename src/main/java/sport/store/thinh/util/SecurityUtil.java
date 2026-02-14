package sport.store.thinh.util;

import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import sport.store.thinh.domain.dto.response.ResLoginDTO;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SecurityUtil {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${thinh.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${thinh.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public SecurityUtil(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public static Optional<String> getCurrentUserLogin() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) return Optional.empty();

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails user) {
            return Optional.of(user.getUsername());
        }
        if (principal instanceof Jwt jwt) {
            return Optional.of(jwt.getSubject());
        }
        if (principal instanceof String s) {
            return Optional.of(s);
        }
        return Optional.empty();
    }

    // ===== TOKEN CREATE =====

    public String createAccessToken(String email, ResLoginDTO resLoginDTO) {

        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenExpiration, ChronoUnit.SECONDS);

        List<String> permissions = List.of(
                "ROLE_USER_CREATE",
                "ROLE_USER_UPDATE"
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("user", resLoginDTO.getUser())
                .claim("permission", permissions)
                .build();

        JwsHeader header = JwsHeader.with(JWT_ALGORITHM).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    public String createRefreshToken(String email, ResLoginDTO resLoginDTO) {

        Instant now = Instant.now();
        Instant expiry = now.plus(refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("user", resLoginDTO.getUser())
                .build();

        JwsHeader header = JwsHeader.with(JWT_ALGORITHM).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    // ===== TOKEN VERIFY =====

    public Jwt checkValidRefreshToken(String refreshToken) {
        return jwtDecoder.decode(refreshToken);
    }
}

