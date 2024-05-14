package roomescape.member.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.List;
import javax.naming.AuthenticationException;
import org.springframework.stereotype.Service;
import roomescape.exceptions.NotFoundException;
import roomescape.login.dto.LoginRequest;
import roomescape.member.domain.Email;
import roomescape.member.domain.LoginMember;
import roomescape.member.domain.Password;
import roomescape.member.dto.LoginMemberRequest;
import roomescape.member.dto.MemberIdNameResponse;
import roomescape.member.dto.MemberNameResponse;
import roomescape.member.repository.MemberRepository;


@Service
public class MemberService {

    private static final String SECRET_KEY = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E";
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public LoginMember getLoginMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원 id입니다. memberId = " + memberId));
    }

    public List<MemberIdNameResponse> findMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberIdNameResponse::new)
                .toList();
    }

    public String createMemberToken(LoginRequest loginRequest) throws AuthenticationException {
        LoginMember loginMember = memberRepository.findByEmail(new Email(loginRequest.email()))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (memberRepository.isCorrectPassword(loginMember.getEmail(), new Password(loginRequest.password()))) {
            return parseToToken(loginMember);
        }
        throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
    }

    private String parseToToken(LoginMember loginMember) {
        return Jwts.builder()
                .setSubject(loginMember.getId().toString())
                .claim("name", loginMember.getName().name())
                .claim("email", loginMember.getEmail().email())
                .claim("role", loginMember.getRole().getDbValue())
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();
    }

    public MemberNameResponse getMemberNameResponseByToken(String token) throws AuthenticationException {
        LoginMember loginMember = parseTokenToLoginMember(token);
        return new MemberNameResponse(loginMember);
    }

    public boolean isAdminToken(String token) throws AuthenticationException {
        LoginMember loginMember = parseTokenToLoginMember(token);

        return loginMember.isAdmin();
    }

    private LoginMember parseTokenToLoginMember(String token) throws AuthenticationException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return new LoginMember(
                    Long.valueOf(claims.getSubject()),
                    (String) claims.get("name"),
                    (String) claims.get("email"),
                    (String) claims.get("role")
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        }
    }

    public LoginMemberRequest getLoginMemberRequestByToken(String token) throws AuthenticationException {
        LoginMember loginMember = parseTokenToLoginMember(token);

        return new LoginMemberRequest(loginMember);
    }
}
