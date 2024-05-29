package roomescape.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.Password;
import roomescape.global.handler.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.service.auth.dto.AuthenticationInfoResponse;
import roomescape.service.auth.dto.TokenRequest;
import roomescape.service.auth.dto.TokenResponse;

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
                .orElseThrow(() -> new NotFoundException(String.format("email값: %s 에 대한 사용자가 존재하지 않습니다.", email)));
    }

    public AuthenticationInfoResponse loginCheck(HttpServletRequest request) {
        String authenticationInfo = tokenProvider.parseAuthenticationInfoFromCookies(request.getCookies());
        return AuthenticationInfoResponse.from(authenticationInfo);
    }
}
