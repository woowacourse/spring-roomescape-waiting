package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.dao.MemberDao;
import roomescape.member.dto.MemberResponse;

@Service
public class MemberService {
    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public List<MemberResponse> findMembers() {
        return memberDao.findMembers()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }
}
