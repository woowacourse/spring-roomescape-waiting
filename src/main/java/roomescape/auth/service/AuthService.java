package roomescape.auth.service;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;
import roomescape.auth.constant.AuthConstant;
import roomescape.member.dao.MemberDao;
import roomescape.member.exception.MemberNotExistException;
import roomescape.member.model.Member;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.exception.InvalidCredentialsException;
import roomescape.global.exception.UnauthorizedException;
import roomescape.auth.infrastructure.JwtTokenProvider;

@Service
public class AuthService {

    private final MemberDao memberDao;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberDao memberDao, JwtTokenProvider jwtTokenProvider) {
        this.memberDao = memberDao;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String login(LoginRequest loginRequest) {
        String email = loginRequest.email();
        String password = loginRequest.password();
        Member member = findMemberByEmailAndPassword(email, password);
        return jwtTokenProvider.createToken(member.getEmail());
    }

    private Member findMemberByEmailAndPassword(String email, String password) {
        return memberDao.findByEmailAndPassword(email, password)
                .orElseThrow(InvalidCredentialsException::new);
    }
}
