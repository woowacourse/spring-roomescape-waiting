package roomescape.reservation.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dao.ReservationDetailDao;
import roomescape.reservation.application.dto.ReservationPageResult;
import roomescape.reservation.application.dto.ReservationResult;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ReservationQueryService {

    private static final int PAGE_SIZE = 20;

    private final ReservationDetailDao reservationDetailDao;

    public ReservationPageResult findAllByPage(int page) {
        long totalElements = reservationDetailDao.countAll();
        List<ReservationResult> content = reservationDetailDao.findAllByPage(PAGE_SIZE,
                        (long) page * PAGE_SIZE).stream()
                .map(ReservationResult::from)
                .toList();

        return ReservationPageResult.of(content, page, PAGE_SIZE, totalElements);
    }

    public List<ReservationResult> findByName(String username) {
        return reservationDetailDao.findByName(username).stream()
                .map(ReservationResult::from)
                .toList();
    }
}
