package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.member.dao.MemberDao;
import roomescape.member.model.Member;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.exception.InvalidCredentialsException;
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
        return jwtTokenProvider.createToken(member);
    }

    private Member findMemberByEmailAndPassword(String email, String password) {
        return memberDao.findByEmailAndPassword(email, password)
                .orElseThrow(InvalidCredentialsException::new);
    }
}
