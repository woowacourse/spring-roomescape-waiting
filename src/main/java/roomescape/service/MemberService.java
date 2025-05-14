package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dao.MemberDao;
import roomescape.entity.Member;
import roomescape.dto.request.MemberRequest;
import roomescape.exception.custom.DuplicatedException;

@Service
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public List<Member> findAllMembers() {
        return memberDao.findAllMembers();
    }

    public Member addMember(MemberRequest request) {
        validateDuplicateMember(request);

        return memberDao.addMember(
            new Member(request.name(), request.email(), request.password()));
    }

    private void validateDuplicateMember(MemberRequest request) {
        if (memberDao.existMemberByEmail(request.email())) {
            throw new DuplicatedException("member");
        }
    }
}
