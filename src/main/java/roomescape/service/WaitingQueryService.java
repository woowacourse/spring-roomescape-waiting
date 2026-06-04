package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.WaitingWithRank;
import roomescape.domain.Waitings;
import roomescape.repository.WaitingDao;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WaitingQueryService {

    private final WaitingDao waitingDao;

    public List<WaitingWithRank> getByName(String name) {
        Member member = new Member(name);
        Waitings waitings = new Waitings(waitingDao.findAllSharingSlotWith(member));
        return waitings.rankedBy(member);
    }
}
