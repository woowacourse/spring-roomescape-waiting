package roomescape.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Password;
import roomescape.repository.MemberRepository;
import roomescape.global.handler.exception.CustomException;
import roomescape.global.handler.exception.ExceptionCode;
import roomescape.infrastructure.TokenProvider;
import roomescape.service.auth.dto.TokenRequest;
import roomescape.service.auth.dto.TokenResponse;
import roomescape.service.auth.dto.AuthenticationInfoResponse;

@Service
public class LoginService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public LoginService(MemberRepository memberRepository, TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    public TokenResponse login(TokenRequest tokenRequest) {
        Member member = findMemberBy(tokenRequest.email(), tokenRequest.password());
        String token = tokenProvider.generateTokenOf(member);
        return TokenResponse.from(token);
    }

    private Member findMemberBy(String email, String password) {
        return memberRepository.findMemberByEmailAndPassword(new Email(email), new Password(password))
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_USER));
    }

    public AuthenticationInfoResponse loginCheck(HttpServletRequest request) {
        String authenticationInfo = tokenProvider.parseAuthenticationInfoFromCookies(request.getCookies());
        return AuthenticationInfoResponse.from(authenticationInfo);
    }
}
