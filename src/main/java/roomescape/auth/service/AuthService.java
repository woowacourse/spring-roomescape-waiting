package roomescape.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.SignUpRequest;
import roomescape.global.auth.jwt.JwtHandler;
import roomescape.global.auth.jwt.dto.TokenDto;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.service.MemberService;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtHandler jwtHandler;

    public AuthService(MemberService memberService, MemberRepository memberRepository, final JwtHandler jwtHandler) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.jwtHandler = jwtHandler;
    }

    @Transactional
    public TokenDto signUp(final SignUpRequest signupRequest) {
        Member member = memberService.addMember(signupRequest);

        return jwtHandler.createToken(member.getId());
    }

    public TokenDto login(final LoginRequest request) {
        Member member = memberService.findMemberByEmailAndPassword(request);

        return jwtHandler.createToken(member.getId());
    }

    public LoginCheckResponse checkLogin(final Long memberId) {
        Member member = memberRepository.getById(memberId);

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
