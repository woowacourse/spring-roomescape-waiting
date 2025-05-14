package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dto.request.LoginRequestDto;
import roomescape.dto.response.MemberResponseDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.model.Member;
import roomescape.repository.MemberRepository;

@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    public AuthService(final JwtProvider jwtProvider, final MemberRepository memberRepository) {
        this.jwtProvider = jwtProvider;
        this.memberRepository = memberRepository;
    }

    public void login(final LoginRequestDto loginRequestDto) {
        Member member = findMemberByEmail(loginRequestDto.email());

        if (!member.hasSamePassword(loginRequestDto.password())) {
            throw new UnauthorizedException("로그인에 실패했습니다.");
        }
    }

    public TokenResponseDto createToken(String email) {
        String token = jwtProvider.createToken(email);
        return new TokenResponseDto(token);
    }

    public MemberResponseDto getMemberByToken(String tokenFromCookie) {
        String payload = jwtProvider.getPayload(tokenFromCookie);
        Member member = findMemberByEmail(payload);

        return new MemberResponseDto(member);
    }

    public Member getAuthenticatedMember(String tokenFromCookie) {
        String payload = jwtProvider.getPayload(tokenFromCookie);
        return findMemberByEmail(payload);
    }

    private Member findMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
    }

}
