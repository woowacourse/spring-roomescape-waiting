package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.global.auth.jwt.JwtHandler;
import roomescape.global.auth.jwt.dto.TokenDto;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@Service
public class AuthService {
    private final MemberService memberService;
    private final JwtHandler jwtHandler;

    public AuthService(final MemberService memberService, final JwtHandler jwtHandler) {
        this.memberService = memberService;
        this.jwtHandler = jwtHandler;
    }

    public TokenDto login(final LoginRequest request) {
        Member member = memberService.findMemberByEmailAndPassword(request.email(), request.password());

        return jwtHandler.createToken(member.getId());
    }

    public LoginCheckResponse checkLogin(final Long memberId) {
        Member member = memberService.findMemberById(memberId);

        return new LoginCheckResponse(member.getName());
    }

    public TokenDto reissueToken(final String accessToken, final String refreshToken) {
        try {
            jwtHandler.validateToken(refreshToken);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(ErrorType.INVALID_REFRESH_TOKEN, ErrorType.INVALID_REFRESH_TOKEN.getDescription(), e);
        }

        Long memberId = jwtHandler.getMemberIdFromTokenWithNotValidate(accessToken);
        return jwtHandler.createToken(memberId);
    }
}
