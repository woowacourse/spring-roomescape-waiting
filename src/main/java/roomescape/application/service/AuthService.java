package roomescape.application.service;

import org.springframework.stereotype.Service;
import roomescape.application.provider.JwtTokenProvider;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dto.request.LoginRequestDto;
import roomescape.dto.response.MemberResponseDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.model.Member;
import roomescape.repository.MemberRepository;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    public void login(LoginRequestDto loginRequestDto) {
        Member member = findMemberByEmail(loginRequestDto.email());

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
        Member member = findMemberByEmail(payload);

        return new MemberResponseDto(member);
    }

    public Member getAuthenticatedMember(String tokenFromCookie) {
        String payload = jwtTokenProvider.getPayload(tokenFromCookie);
        return findMemberByEmail(payload);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
    }

}
