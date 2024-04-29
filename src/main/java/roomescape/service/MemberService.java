package roomescape.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.dto.LogInRequest;
import roomescape.dto.MemberPreviewResponse;
import roomescape.infrastructure.JdbcMemberRepository;
import roomescape.service.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class MemberService {

    @Value("${jwt.secret-key.prod}")
    private String secretKey;

    private final MemberRepository memberRepository;

    public MemberService(JdbcMemberRepository jdbcMemberRepository) {
        this.memberRepository = jdbcMemberRepository;
    }

    public String logIn(LogInRequest logInRequest) {
        String email = logInRequest.email();
        String password = logInRequest.password();

        Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new ResourceNotFoundException("일치하는 이메일과 비밀번호가 없습니다."));

        return Jwts.builder().subject(member.getId().toString())
                .claim("role", member.getRole().name())
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public Member getUserByToken(String token) {
        Long memberId = Long.valueOf(
                Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject());

        return findValidatedSiteUserById(memberId);
    }

    private Member findValidatedSiteUserById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new ResourceNotFoundException("아이디에 해당하는 사용자가 없습니다."));
    }

    public List<MemberPreviewResponse> getAllMemberPreview() {
        return memberRepository.findAll().stream()
                .map(MemberPreviewResponse::from)
                .toList();
    }
}
