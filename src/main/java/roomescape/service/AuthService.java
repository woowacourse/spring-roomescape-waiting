package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.domain.LoginMember;
import roomescape.domain.Member;
import roomescape.dto.request.LoginRequestDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.repository.MemberRepository;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public TokenResponseDto login(final LoginRequestDto loginRequestDto) {
        final Member member = findMemberByEmail(loginRequestDto.email());

        if (!member.hasSamePassword(loginRequestDto.password())) {
            throw new UnauthorizedException("로그인에 실패했습니다.");
        }

        return createToken(loginRequestDto.email());
    }

    private TokenResponseDto createToken(final String email) {
        final String token = jwtProvider.createToken(email);
        return new TokenResponseDto(token);
    }

    public LoginMember getMemberByToken(final String tokenFromCookie) {
        final String payload = jwtProvider.getPayload(tokenFromCookie);
        final Member member = findMemberByEmail(payload);

        return LoginMember.from(member);
    }

    public Member getAuthenticatedMember(final String tokenFromCookie) {
        final String payload = jwtProvider.getPayload(tokenFromCookie);
        return findMemberByEmail(payload);
    }

    private Member findMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
    }

}
