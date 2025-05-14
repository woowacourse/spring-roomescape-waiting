package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.dao.MemberDao;
import roomescape.entity.Member;
import roomescape.dto.request.LoginRequest;
import roomescape.exception.custom.AuthenticatedException;
import roomescape.provider.JwtTokenProvider;

@Service
public class AuthService {

    private final MemberDao memberDao;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberDao memberDao, JwtTokenProvider jwtTokenProvider) {
        this.memberDao = memberDao;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String createToken(LoginRequest request) {
        Member member = memberDao.findMemberByEmail(request.email());

        validatePassword(request.password(), member);
        return jwtTokenProvider.createToken(member);
    }

    private void validatePassword(String password, Member member) {
        if(!password.equals(member.getPassword())) {
            throw new AuthenticatedException();
        }
    }

    public Member findMemberByToken(String token) {
        Long memberId = jwtTokenProvider.getMemberIdFromToken(token);
        return memberDao.findMemberById(memberId);
    }
}
