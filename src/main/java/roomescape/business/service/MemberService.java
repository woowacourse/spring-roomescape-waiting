package roomescape.business.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.persistence.dao.MemberDao;
import roomescape.presentation.dto.MemberResponse;

@Service
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(final MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public List<MemberResponse> findAll() {
        return memberDao.findAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }
}
