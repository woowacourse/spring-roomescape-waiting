package roomescape.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.exception.auth.AuthenticationException;
import roomescape.exception.auth.UnauthorizedException;

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
