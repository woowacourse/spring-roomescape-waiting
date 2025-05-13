package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.repository.MemberDao;
import roomescape.member.service.dto.MemberInfo;

@Service
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(final MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public List<MemberInfo> findAll() {
        return memberDao.findAll()
                .stream()
                .map(MemberInfo::new)
                .toList();
    }
}
