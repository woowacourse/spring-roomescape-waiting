package roomescape.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.exception.AuthenticationException;
import roomescape.service.dto.AuthInfo;

@Component
public class JwtTokenManager {

    private static final String NAME_KEY = "name";
    private static final String ROLE_KEY = "role";

    private final JwtProperties jwtProperties;

    public JwtTokenManager(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createToken(Member member) {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + jwtProperties.getExpirationMillis());

        return Jwts.builder()
                .setSubject(member.getId().toString())
                .claim(NAME_KEY, member.getName())
                .claim(ROLE_KEY, member.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes()))
                .compact();
    }

    public AuthInfo getAuthInfo(String token) {
        Claims claims;
        try {
            claims = getClaims(token);
        } catch (AuthenticationException | IllegalArgumentException | JwtException e) {
            throw new AuthenticationException(e.getMessage());
        }

        Long id = Long.valueOf(claims.getSubject());
        String name = claims.get(NAME_KEY).toString();
        Role role = Role.findBy(claims.get(ROLE_KEY).toString());

        return new AuthInfo(id, name, role);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
