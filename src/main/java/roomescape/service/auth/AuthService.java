package roomescape.service.auth;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.AuthInfo;
import roomescape.service.dto.request.LoginRequest;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenManager jwtTokenManager;
    private final TokenCookieManager tokenCookieManager;

    public AuthService(MemberRepository memberRepository, JwtTokenManager jwtTokenManager,
                       TokenCookieManager tokenCookieManager) {
        this.memberRepository = memberRepository;
        this.jwtTokenManager = jwtTokenManager;
        this.tokenCookieManager = tokenCookieManager;
    }

    public ResponseCookie login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmailAndPassword(loginRequest.email(), loginRequest.password())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        String token = jwtTokenManager.createToken(member);
        return tokenCookieManager.createTokenCookie(token);
    }

    public ResponseCookie logout() {
        return tokenCookieManager.expireTokenCookie();
    }

    public AuthInfo getAuthInfo(Cookie[] cookies) {
        String token = tokenCookieManager.extractTokenBy(cookies);
        return jwtTokenManager.getAuthInfo(token);
    }
}
