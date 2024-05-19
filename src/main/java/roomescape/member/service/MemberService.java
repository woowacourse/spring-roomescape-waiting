package roomescape.member.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.List;
import javax.naming.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberIdNameResponse;
import roomescape.member.dto.MemberNameResponse;
import roomescape.member.dto.MemberRequest;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;

    public MemberService(MemberRepository MemberRepository) {
        this.memberRepository = MemberRepository;
    }

    public List<MemberIdNameResponse> findMembersIdAndName() {
        return memberRepository.findAll()
                .stream()
                .map(MemberIdNameResponse::new)
                .toList();
    }

    public MemberNameResponse getMemberNameResponseByToken(String token) throws AuthenticationException {
        Member member = parseTokenToMember(token);
        return new MemberNameResponse(member);
    }

    public boolean isAdminToken(String token) throws AuthenticationException {
        Member member = parseTokenToMember(token);

        return member.isAdmin();
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

    public MemberRequest getMemberRequestByToken(String token) throws AuthenticationException {
        Member member = parseTokenToMember(token);

        return new MemberRequest(member);
    }
}
