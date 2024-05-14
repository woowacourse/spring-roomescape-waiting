package roomescape.auth.core.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.util.Date;
import org.springframework.stereotype.Component;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

@Component
public class JwtTokenProvider implements TokenProvider {
    private static final String MEMBER_ID_CLAIM = "memberId";
    private static final String MEMBER_ROLE_CLAIM = "memberRole";

    private final TokenProperties tokenProperties;

    public JwtTokenProvider(final TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    public String createToken(final Member member) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenProperties.getValidityInMilliseconds());
        return Jwts.builder()
                .setSubject(member.getId().toString())
                .setIssuedAt(now)
                .setExpiration(validity)
                .claim(MEMBER_ID_CLAIM, member.getName())
                .claim(MEMBER_ROLE_CLAIM, member.getMemberRole())
                .signWith(SignatureAlgorithm.HS256, tokenProperties.getSecretKey())
                .compact();
    }

    public AuthInfo extractAuthInfo(final String token) {
        Claims claims = getClaims(token);
        return new AuthInfo(
                Long.parseLong(claims.getSubject()),
                claims.get(MEMBER_ID_CLAIM, String.class),
                MemberRole.valueOf(claims.get(MEMBER_ROLE_CLAIM, String.class)));
    }

    private Claims getClaims(final String token) {
        try {
            return Jwts.parser().setSigningKey(tokenProperties.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException e) {
            throw new SecurityException("토큰의 형식이 유효하지 않습니다. 다시 로그인해주세요.");
        } catch (SignatureException e) {
            throw new SecurityException("토큰의 값을 인증할 수 없습니다. 다시 로그인해주세요.");
        } catch (ExpiredJwtException e) {
            throw new SecurityException("토큰이 만료되었습니다. 다시 로그인해주세요.");
        } catch (JwtException e) {
            throw new SecurityException("토큰 오류입니다. 다시 로그인해주세요.");
        }
    }
}
