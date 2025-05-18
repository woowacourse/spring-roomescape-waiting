package roomescape.business.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.domain.Member;
import roomescape.exception.UnauthorizedException;
import roomescape.persistence.repository.MemberRepository;
import roomescape.presentation.dto.LoginMember;

@Service
public class AuthService {

    private final static String secretKey = "ThisSecretKeyIsOnlyUseLearningTestSoIsNotImportant1234567890Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";

    private final JwtParser jwtParser;
    private final MemberRepository memberRepository;

    public AuthService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        jwtParser = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build();
    }

    @Transactional(readOnly = true)
    public String login(final String email, final String password) {
        final Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new UnauthorizedException("해당하는 사용자를 찾을 수 없습니다. email: %s".formatted(email)));
        return createAccessToken(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }

    private String createAccessToken(final Long id, final String name, final String email, final String role) {
        return Jwts.builder()
                .claim("id", id)
                .claim("name", name)
                .claim("email", email)
                .claim("role", role)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public LoginMember getLoginMemberByAccessToken(final String accessToken) {
        final Jws<Claims> claimsJws = parseClaims(accessToken);
        final Long id = claimsJws.getPayload()
                .get("id", Long.class);
        final String name = claimsJws.getPayload()
                .get("name", String.class);
        final String email = claimsJws.getPayload()
                .get("email", String.class);
        final String role = claimsJws.getPayload()
                .get("role", String.class);
        return new LoginMember(id, name, email, role);
    }

    private Jws<Claims> parseClaims(final String accessToken) {
        try {
            return jwtParser.parseSignedClaims(accessToken);
        } catch (JwtException e) {
            throw new UnauthorizedException("유효하지 않은 AccessToken 입니다.");
        }
    }
}
