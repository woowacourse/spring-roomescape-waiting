package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.system.auth.jwt.JwtHandler;
import roomescape.system.auth.jwt.dto.TokenDto;
import roomescape.system.exception.error.ErrorType;
import roomescape.system.exception.model.UnauthorizedException;
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
        final Member member = memberService.findMemberByEmailAndPassword(request.email(), request.password());

        return jwtHandler.createToken(member.getId());
    }

    public LoginCheckResponse checkLogin(final Long memberId) {
        final Member member = memberService.findMemberById(memberId);

        return new LoginCheckResponse(member.getName());
    }

    public TokenDto reissueToken(final String accessToken, final String refreshToken) {
        try {
            jwtHandler.validateToken(refreshToken);
        } catch (final UnauthorizedException e) {
            throw new UnauthorizedException(ErrorType.INVALID_REFRESH_TOKEN,
                    ErrorType.INVALID_REFRESH_TOKEN.getDescription(), e);
        }

        final Long memberId = jwtHandler.getMemberIdFromTokenWithNotValidate(accessToken);
        return jwtHandler.createToken(memberId);
    }
}
