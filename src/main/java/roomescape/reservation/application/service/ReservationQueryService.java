package roomescape.reservation.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dao.ReservationDetailDao;
import roomescape.reservation.application.dto.ReservationApplicationPageResult;
import roomescape.reservation.application.dto.ReservationApplicationResult;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReservationQueryService {

    private static final int PAGE_SIZE = 20;

    private final ReservationDetailDao reservationDetailDao;

    public ReservationApplicationPageResult findAllByPage(int page) {
        long totalElements = reservationDetailDao.countAll();
        List<ReservationApplicationResult> content = reservationDetailDao.findAllByPage(PAGE_SIZE,
                        (long) page * PAGE_SIZE).stream()
                .map(ReservationApplicationResult::from)
                .toList();

        return ReservationApplicationPageResult.of(content, page, PAGE_SIZE, totalElements);
    }

    public List<ReservationApplicationResult> findByName(String username) {
        return reservationDetailDao.findByName(username).stream()
                .map(ReservationApplicationResult::from)
                .toList();
    }
}
