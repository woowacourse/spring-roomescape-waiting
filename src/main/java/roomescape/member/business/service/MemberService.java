package roomescape.member.business.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.impl.NotFoundException;
import roomescape.member.business.domain.Member;
import roomescape.member.business.repository.MemberDao;
import roomescape.member.presentation.response.MemberResponse;

@Service
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(final MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public List<MemberResponse> findAll() {
        final List<Member> members = memberDao.findAll();
        return members.stream()
                .map(MemberResponse::of)
                .toList();
    }

    public MemberResponse findById(final Long id) {
        return MemberResponse.of(memberDao.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 사용자가 없습니다.")));
    }
}
