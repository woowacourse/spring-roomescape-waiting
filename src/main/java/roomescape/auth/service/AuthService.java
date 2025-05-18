package roomescape.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginMember;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.infrastructure.TokenProvider;
import roomescape.exception.ForbiddenException;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public void checkAdmin(final HttpServletRequest request) {
        final LoginMember loginMember = extractMemberByRequest(request);
        if (!loginMember.isAdmin()) {
            throw new ForbiddenException("관리자 권한이 필요합니다.");
        }
    }

    public LoginMember extractMemberByRequest(final HttpServletRequest request) {
        try {
            return findMemberByMemberId(extractMemberId(request));
        } catch (IllegalArgumentException | NotFoundException e) {
            log.debug(e.getMessage());
            throw new UnauthorizedException("인증에 실패했습니다.");
        }
    }

    private Long extractMemberId(final HttpServletRequest request) {
        final Object raw = request.getAttribute(MEMBER_ID);
        return Optional.ofNullable(raw)
                .filter(Long.class::isInstance)
                .map(Long.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("memberId 형식이 올바르지 않습니다. memberId =" + raw));
    }

    private LoginMember findMemberByMemberId(final Long memberId) {
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다. memberId =" + memberId));
        return new LoginMember(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }
}
