package roomescape.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.dto.request.LogInRequest;
import roomescape.dto.response.MemberPreviewResponse;
import roomescape.dto.response.MemberReservationResponse;
import roomescape.service.exception.ResourceNotFoundException;

@Service
public class MemberService {

    // TODO: 토큰 관련 로직 분리
    @Value("${jwt.secret-key.prod}")
    private String secretKey;

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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

    public List<MemberReservationResponse> getReservations(Member member) {
        return member.getReservations()
                .stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
