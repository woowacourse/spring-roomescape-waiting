package roomescape.member.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.dao.MemberDao;
import roomescape.member.exception.MemberExceptionCode;
import roomescape.member.role.MemberRole;

@Repository
public class MemberRepository {

    private final MemberDao memberDao;

    public MemberRepository(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public long findIdByEmailAndPassword(String email, String password) {
        return memberDao.findIdByEmailAndPassword(email, password)
                .orElseThrow(() -> new RoomEscapeException(MemberExceptionCode.ID_AND_PASSWORD_NOT_MATCH_OR_EXIST));
    }

    public String findNameById(long id) {
        return memberDao.findNameById(id)
                .orElseThrow(() -> new RoomEscapeException(MemberExceptionCode.MEMBER_NOT_EXIST_EXCEPTION));
    }

    public List<Long> findAllId() {
        return memberDao.findAllId();
    }

    public MemberRole findRoleById(long id) {
        String memberRole = memberDao.findRoleById(id)
                .orElseThrow(() -> new RoomEscapeException(MemberExceptionCode.MEMBER_ROLE_NOT_EXIST_EXCEPTION));
        return MemberRole.findMemberRole(memberRole);
    }
}
