package roomescape.infrastructure.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.RequiredTypeException;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import roomescape.domain.member.Reserver;
import roomescape.domain.member.Role;
import roomescape.exception.auth.AuthenticationException;
import roomescape.infrastructure.auth.member.UserInfo;

public class JwtTokenProvider {

    private static final String COOKIE_KEY = "token";
    private static final String USERNAME_KEY = "username";
    private static final String NAME_KEY = "name";
    private static final String ROLE_KEY = "role";
    private static final int ONE_HOUR = 3600000;

    private final String secretKey;

    public JwtTokenProvider(String secretKey) {
        this.secretKey = secretKey;
    }

    public String createToken(Reserver reserver) {
        return Jwts.builder()
                .setSubject(reserver.getId().toString())
                .claim(USERNAME_KEY, reserver.getUsername())
                .claim(NAME_KEY, reserver.getName())
                .claim(ROLE_KEY, reserver.getRole())
                .setExpiration(new Date(System.currentTimeMillis() + ONE_HOUR))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public UserInfo resolveToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long id = Long.parseLong(claims.getSubject());
            String username = (String) claims.get(USERNAME_KEY);
            String name = (String) claims.get(NAME_KEY);
            Role role = Role.valueOf((String) claims.get(ROLE_KEY));
            return new UserInfo(id, username, name, role);
        } catch (RequiredTypeException | IllegalArgumentException e) {
            throw new AuthenticationException("토큰 파싱에 실패하였습니다");
        } catch (Exception e) {
            throw new AuthenticationException("토큰 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public static String getCookieKey() {
        return COOKIE_KEY;
    }
}
