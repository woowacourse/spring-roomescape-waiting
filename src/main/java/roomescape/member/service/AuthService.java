package roomescape.member.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.member.controller.request.TokenLoginCreateRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.controller.response.TokenLoginResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.JwtTokenProvider;
import roomescape.member.repository.MemberRepository;
import roomescape.member.resolver.UnauthenticatedException;

@Service
public class AuthService {

    private static final String TOKEN = "token";
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    public TokenLoginResponse tokenLogin(TokenLoginCreateRequest tokenLoginCreateRequest) {
        Email email = new Email(tokenLoginCreateRequest.email());
        Password password = new Password(tokenLoginCreateRequest.password());

        if (memberRepository.existsByEmailAndPassword(email, password)) {
            String accessToken = jwtTokenProvider.createToken(tokenLoginCreateRequest.email());
            return new TokenLoginResponse(accessToken);
        }
        throw new IllegalArgumentException("[ERROR] 아이디 또는 비밀번호를 올바르게 입력해주세요.");
    }

    public MemberResponse findUserByToken(String token) {
        String payload = jwtTokenProvider.getPayload(token);
        return MemberResponse.from(
                memberRepository.findByEmail(new Email(payload))
                        .orElseThrow(() -> new NoSuchElementException("[ERROR] 멤버가 존재하지 않습니다.")));
    }

    public String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new UnauthenticatedException("[ERROR] 로그인 정보가 유효하지 않습니다.");
        }

        String token = extractTokenFromCookie(cookies);
        if (token == null) {
            throw new UnauthenticatedException("[ERROR] 로그인 정보가 유효하지 않습니다.");
        }
        return token;
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (TOKEN.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
