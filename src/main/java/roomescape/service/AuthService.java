package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dao.MemberDao;
import roomescape.dto.request.LoginRequestDto;
import roomescape.dto.response.MemberResponseDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.model.Member;

@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final MemberDao memberDao;

    public AuthService(JwtProvider jwtProvider, MemberDao memberDao) {
        this.jwtProvider = jwtProvider;
        this.memberDao = memberDao;
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
        return memberDao.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
    }

}
