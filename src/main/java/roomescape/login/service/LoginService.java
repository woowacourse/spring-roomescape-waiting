package roomescape.login.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.naming.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import roomescape.exceptions.NotFoundException;
import roomescape.login.dto.LoginRequest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.dto.MemberRequest;
import roomescape.member.repository.MemberRepository;

@Service
public class LoginService {

    private final MemberRepository memberRepository;

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    public LoginService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String createMemberToken(LoginRequest loginRequest) throws AuthenticationException {
        Member member = memberRepository.findByEmail(new Email(loginRequest.email()))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (member.isPassword(new Password(loginRequest.password()))) {
            return parseToToken(member);
        }
        throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
    }

    private String parseToToken(Member member) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusSeconds(validityInMilliseconds);

        return Jwts.builder()
                .setSubject(member.getId().toString())
                .claim("name", member.getName().name())
                .claim("email", member.getEmail().email())
                .claim("role", member.getRole().name())
                .claim("password", member.getPassword().password())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public MemberRequest getMemberRequestByToken(String token) throws AuthenticationException {
        Member member = parseTokenToMember(token);

        return new MemberRequest(member);
    }

    private Member parseTokenToMember(String token) throws AuthenticationException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return new Member(
                    Long.valueOf(claims.getSubject()),
                    (String) claims.get("name"),
                    (String) claims.get("email"),
                    (String) claims.get("role"),
                    (String) claims.get("password")
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        }
    }
}
