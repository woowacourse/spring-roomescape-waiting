package roomescape.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.application.provider.JwtTokenProvider;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dto.request.LoginRequestDto;
import roomescape.dto.response.MemberResponseDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.model.Member;
import roomescape.persistence.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public void login(LoginRequestDto loginRequestDto) {
        Member member = memberRepository.findByEmail(loginRequestDto.email());

        if (!member.hasSamePassword(loginRequestDto.password())) {
            throw new UnauthorizedException("로그인에 실패했습니다.");
        }
    }

    public TokenResponseDto createToken(String email) {
        String token = jwtTokenProvider.createToken(email);
        return new TokenResponseDto(token);
    }

    public MemberResponseDto getMemberByToken(String tokenFromCookie) {
        String payload = jwtTokenProvider.getPayload(tokenFromCookie);
        Member member = memberRepository.findByEmail(payload);

        return new MemberResponseDto(member);
    }

    public Member getAuthenticatedMember(String tokenFromCookie) {
        String payload = jwtTokenProvider.getPayload(tokenFromCookie);
        return memberRepository.findByEmail(payload);
    }

}
