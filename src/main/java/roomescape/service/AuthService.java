package roomescape.service;

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
}
