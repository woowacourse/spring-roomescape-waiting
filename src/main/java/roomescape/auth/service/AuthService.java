package roomescape.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.infrastructure.TokenProvider;
import roomescape.error.NotFoundException;
import roomescape.error.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String MEMBER_ID = "memberId";

    private final TokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public String createToken(final LoginRequest loginRequest) {
        final Member member = memberRepository.findByEmailAndPassword(loginRequest.email(), loginRequest.password())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 패스워드가 올바르지 않습니다."));
        return jwtTokenProvider.createToken(createClaims(member));
    }

    private Claims createClaims(final Member member) {
        return Jwts.claims()
                .subject(member.getId().toString())
                .build();
    }

    public LoginCheckResponse checkLogin(final String token) {
        final Long memberId = parseMemberId(token);
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UnauthorizedException("유효하지 않은 회원입니다. id: " + memberId));
        return new LoginCheckResponse(member);
    }

    private Long parseMemberId(final String token) {
        try {
            return Long.valueOf(jwtTokenProvider.extractPrincipal(token));
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    /**
     * @throws IllegalArgumentException 유효하지 않은 memberId 형식이거나 요청 속성에 memberId가 없을 경우
     * @throws NotFoundException        해당 memberId의 회원을 찾지 못한 경우
     */
    public Member extractMemberByRequest(final HttpServletRequest request) {
        return findMemberByMemberId(extractMemberId(request));
    }

    private Long extractMemberId(final HttpServletRequest request) {
        final Object raw = request.getAttribute(MEMBER_ID);
        return Optional.ofNullable(raw)
                .filter(Long.class::isInstance)
                .map(Long.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("memberId 형식이 올바르지 않습니다. memberId =" + raw));
    }

    private Member findMemberByMemberId(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다. memberId =" + memberId));
    }
}
