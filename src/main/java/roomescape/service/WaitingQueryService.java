package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.WaitingDao;
import roomescape.dao.dto.WaitingWithRank;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingQueryService {

    private final WaitingDao waitingDao;

    public List<WaitingWithRank> getByName(String name) {
        return waitingDao.findAllByName(name);
    }
}
