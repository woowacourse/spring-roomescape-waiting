package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Slot;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.dao.WaitingDao;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaitingQueryService {

    private final WaitingDao waitingDao;

    public List<Waiting> getBySlot(Slot slot) {
        return waitingDao.findAllBySlot(slot);
    }

    public List<WaitingWithRank> getByName(String name) {
        return waitingDao.findAllByName(name);
    }
}
