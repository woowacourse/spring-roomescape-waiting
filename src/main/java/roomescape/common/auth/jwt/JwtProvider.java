package roomescape.common.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Role;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    private final Key key;

    @Value("${roomescape.auth.jwt.access_expiration}")
    private Long tokenExpiration;

    public JwtProvider(@Value("${roomescape.auth.jwt.key}") String key) {
        this.key = Keys.hmacShaKeyFor(key.getBytes());
    }

    public String generateToken(Long id, String name, Role role) {
        return Jwts.builder()
                .claim("id", String.valueOf(id))
                .claim("name", name)
                .claim("role", role.name())
                .setSubject("Authorization")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
