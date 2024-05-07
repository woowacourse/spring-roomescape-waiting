package roomescape.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;
import roomescape.infrastructure.TokenProvider;
import roomescape.service.dto.request.TokenRequest;
import roomescape.service.dto.response.AuthenticationInfoResponse;
import roomescape.service.dto.response.TokenResponse;

@Service
public class LoginService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public LoginService(MemberRepository memberRepository, TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    public TokenResponse login(TokenRequest tokenRequest) {
        Member member = findMemberBy(tokenRequest.email());
        String token = tokenProvider.generateTokenOf(member);
        return TokenResponse.from(token);
    }

    private Member findMemberBy(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_USER));
    }

    public AuthenticationInfoResponse loginCheck(HttpServletRequest request) {
        String authenticationInfo = tokenProvider.parseAuthenticationInfoFromCookies(request.getCookies());
        return AuthenticationInfoResponse.from(authenticationInfo);
    }
}
