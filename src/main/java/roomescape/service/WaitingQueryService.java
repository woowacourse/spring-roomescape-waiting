package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.WaitingWithRank;
import roomescape.repository.WaitingDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WaitingQueryService {

    private final WaitingDao waitingDao;

    public List<WaitingWithRank> getByName(String name) {
        return waitingDao.findAllByName(name);
    }
}
