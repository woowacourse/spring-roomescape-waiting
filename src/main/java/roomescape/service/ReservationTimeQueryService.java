package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeDao;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationTimeQueryService {

    private final ReservationTimeDao reservationTimeDao;

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeDao.findAllReservationTimes().stream()
                .toList();
    }

    public List<ReservationTime> findAvailableReservationTimes(LocalDate date, long themeId) {
        return reservationTimeDao.findAvailableReservationTimes(date, themeId).stream()
                .toList();
    }
}
