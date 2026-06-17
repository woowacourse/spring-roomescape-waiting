package roomescape.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.MemberDao;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public List<Member> findUsers() {
        return memberDao.findByRole(Role.USER);
    }

    public Member getMemberById(Long id) {
        return memberDao.findById(id).orElseThrow(()
                -> new RoomescapeException(DomainErrorCode.INVALID_INPUT, "해당 ID의 회원이 존재하지 않습니다. ID: " + id)
        );
    }
}
