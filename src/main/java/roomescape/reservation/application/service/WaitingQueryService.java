package roomescape.reservation.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dao.WaitingDetailDao;
import roomescape.reservation.application.dto.WaitingResult;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class WaitingQueryService {

    private final WaitingDetailDao waitingDetailDao;

    public List<WaitingResult> findByName(String username) {
        return waitingDetailDao.findByName(username).stream()
                .map(WaitingResult::from)
                .toList();
    }
}
