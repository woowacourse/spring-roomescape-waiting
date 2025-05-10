package roomescape.auth.service;

import jakarta.servlet.http.Cookie;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.member.dao.MemberDao;
import roomescape.member.model.Member;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.exception.InvalidCredentialsException;
import roomescape.global.exception.UnauthorizedException;
import roomescape.global.infrastructure.JwtTokenProvider;

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
        throw new UnauthorizedException("로그인 되지 않은 상태이다.");
    }

    public boolean isAdminRequest(Cookie[] cookies) {
        String accessToken = extractTokenFromCookie(cookies);
        String email = jwtTokenProvider.getPayload(accessToken);
        Member member =  memberDao.findByEmail(email);
        return member.isAdmin();
    }
}
