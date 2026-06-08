package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationWaitingDao;
import roomescape.domain.service.WaitingWithRank;
import roomescape.domain.common.UserName;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.service.WaitingRanker;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingQueryService {

    private final ReservationWaitingDao waitingDao;
    private final WaitingRanker waitingRanker;

    public List<WaitingWithRank> getByName(UserName name) {
        List<ReservationWaiting> waitings = waitingDao.findAllByName(name);

        return waitings.stream()
                .map(waiting -> {
                    List<ReservationWaiting> allWaitingsInSlot = waitingDao.findAllBySlot(waiting.getSlot());
                    return waitingRanker.getWaitingWithRank(allWaitingsInSlot, waiting);
                })
                .toList();
    }
}
