package roomescape.member.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expiration}")
    private long TOKEN_VALIDITY_IN_MILLISECONDS;

    @Value("${jwt.key}")
    private String SECRET_KEY;

    public String generateToken(Account account) {
        Member member = account.getMember();
        return  Jwts.builder()
                .setSubject(member.getId().toString())
                .claim("name", member.getName().getValue())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_IN_MILLISECONDS))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}
