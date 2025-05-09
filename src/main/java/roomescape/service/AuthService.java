package roomescape.service;

import jakarta.servlet.http.Cookie;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.dto.request.LoginRequest;
import roomescape.exception.InvalidCredentialsException;
import roomescape.infrastructure.JwtTokenProvider;

@Service
public class AuthService {

    private final MemberDao memberDao;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberDao memberDao, JwtTokenProvider jwtTokenProvider) {
        this.memberDao = memberDao;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String createToken(LoginRequest loginRequest) {
        String email = loginRequest.email();
        String password = loginRequest.password();
        Member member = findMember(email, password);
        return jwtTokenProvider.createToken(member.getEmail());
    }

    private Member findMember(String email, String password) {
        try {
            return memberDao.findByEmailAndPassword(email, password);
        } catch (DataIntegrityViolationException exception) {
            throw new InvalidCredentialsException();
        }
    }

    public Member checkAuthenticationStatus(Cookie[] cookies) {
        String accessToken = extractTokenFromCookie(cookies);
        String email = jwtTokenProvider.getPayload(accessToken);
        return memberDao.findByEmail(email);
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        // TODO: 토큰이 없는 상태
        return "";
    }

    public Member checkAuthenticationStatus(String[] cookies) {
        String accessToken = extractTokenFromCookie(cookies);
        String email = jwtTokenProvider.getPayload(accessToken);
        return memberDao.findByEmail(email);
    }

    private String extractTokenFromCookie(String[] cookies) {
        for (String cookie : cookies) {
            String[] cookieEntry = cookie.split("=");
            if (cookieEntry[0].equals("token")) {
                return cookieEntry[1];
            }
        }
        // TODO: 토큰이 없는 상태
        return "";
    }
}
