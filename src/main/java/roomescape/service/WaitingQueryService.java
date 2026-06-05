package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationWaitingDao;
import roomescape.dao.dto.WaitingWithRank;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingQueryService {

    private final ReservationWaitingDao waitingDao;

    public List<WaitingWithRank> getByName(String name) {
        return waitingDao.findAllByName(name);
    }
}
