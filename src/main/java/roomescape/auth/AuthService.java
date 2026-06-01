package roomescape.auth;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.MemberDao;
import roomescape.member.Member;
import roomescape.auth.exception.AuthenticationException;
import roomescape.auth.exception.UnauthorizedException;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberDao memberDao;

    public AuthService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Member checkValidLogin(String email, String password) {
        Member member;
        try {
            member = memberDao.findByEmail(email);
        } catch (EmptyResultDataAccessException e) {
            throw new AuthenticationException();
        }
        if (!member.matchesPassword(password)) {
            throw new AuthenticationException();
        }
        return member;
    }

    public Member findCurrentMember(Long memberId) {
        try {
            return memberDao.findById(memberId);
        } catch (EmptyResultDataAccessException e) {
            throw new UnauthorizedException();
        }
    }
}
