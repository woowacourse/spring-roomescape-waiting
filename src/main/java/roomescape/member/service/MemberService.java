package roomescape.member.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import roomescape.exceptions.NotFoundException;
import roomescape.login.dto.LoginRequest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.dto.LoginMemberRequest;
import roomescape.member.dto.MemberIdNameResponse;
import roomescape.member.dto.MemberNameResponse;
import roomescape.member.repository.MemberJpaRepository;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;


@Service
public class MemberService {

    private static final String SECRET_KEY = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E";
    private final MemberJpaRepository memberJpaRepository;

    public MemberService(MemberJpaRepository MemberJpaRepository) {
        this.memberJpaRepository = MemberJpaRepository;
    }

    public Member getLoginMemberById(Long memberId) {
        return memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원 id입니다. memberId = " + memberId));
    }

    public List<MemberIdNameResponse> findMembers() {
        List<MemberIdNameResponse> memberIdNameResponses = new ArrayList<>();
        for (Member member : memberJpaRepository.findAll()) {
            memberIdNameResponses.add(new MemberIdNameResponse(member));
        }
        return memberIdNameResponses;
    }

    public String createMemberToken(LoginRequest loginRequest) throws AuthenticationException {
        Member member = memberJpaRepository.findByEmail(new Email(loginRequest.email()))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));

        if (member.isPassword(new Password(loginRequest.password()))) {
            return parseToToken(member);
        }
        throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
    }

    private String parseToToken(Member member) {
        return Jwts.builder()
                .setSubject(member.getId().toString())
                .claim("name", member.getName().name())
                .claim("email", member.getEmail().email())
                .claim("role", member.getRole().name())
                .claim("password", member.getPassword().password())
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();
    }

    public MemberNameResponse getMemberNameResponseByToken(String token) throws AuthenticationException {
        Member member = parseTokenToLoginMember(token);
        return new MemberNameResponse(member);
    }

    public boolean isAdminToken(String token) throws AuthenticationException {
        Member member = parseTokenToLoginMember(token);

        return member.isAdmin();
    }

    private Member parseTokenToLoginMember(String token) throws AuthenticationException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
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

    public LoginMemberRequest getLoginMemberRequestByToken(String token) throws AuthenticationException {
        Member member = parseTokenToLoginMember(token);

        return new LoginMemberRequest(member);
    }

    public Member getById(Long memberId) {
        return memberJpaRepository.findById(memberId).orElseThrow(() -> new NotFoundException("id에 맞는 멤버가 없습니다. memberId = " + memberId));
    }
}
