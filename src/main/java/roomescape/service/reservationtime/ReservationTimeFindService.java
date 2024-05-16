package roomescape.service.reservationtime;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationStatuses;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeFindService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeFindService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTime> findReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    public ReservationStatuses findIsBooked(LocalDate date, long themeId) {
        List<ReservationTime> reservedTimes = reservationTimeRepository.findReservationByThemeIdAndDate(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return ReservationStatuses.of(reservedTimes, reservationTimes);
    }
}
