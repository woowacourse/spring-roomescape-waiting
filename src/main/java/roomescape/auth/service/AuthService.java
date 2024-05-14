package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.auth.controller.dto.SignUpRequest;
import roomescape.auth.controller.dto.TokenResponse;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberSignUp;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public AuthService(MemberRepository memberRepository, TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    public void authenticate(LoginRequest loginRequest) {
        if (!memberRepository.existsBy(loginRequest.email(), loginRequest.password())) {
            throw new BusinessException(ErrorType.MEMBER_NOT_FOUND);
        }
    }

    public TokenResponse createToken(LoginRequest loginRequest) {
        return new TokenResponse(tokenProvider.createAccessToken(loginRequest.email()));
    }

    public AuthInfo fetchByToken(String token) {
        Member member = memberRepository.findBy(tokenProvider.getPayload(token).getValue())
                .orElseThrow(() -> new BusinessException(ErrorType.TOKEN_PAYLOAD_EXTRACTION_FAILURE));
        return AuthInfo.of(member);
    }

    public void signUp(SignUpRequest signUpRequest) {
        if (memberRepository.existsBy(signUpRequest.email())) {
            throw new BusinessException(ErrorType.DUPLICATED_EMAIL_ERROR);
        }
        memberRepository.save(
                new MemberSignUp(signUpRequest.name(), signUpRequest.email(), signUpRequest.password(), Role.USER));
    }
}
