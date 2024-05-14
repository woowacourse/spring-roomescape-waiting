package roomescape.service.reservationtime;

import org.springframework.stereotype.Service;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationTimeFindService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeFindService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTime> findReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    public ReservationStatus findIsBooked(LocalDate date, long themeId) {
        List<ReservationTime> reservedTimes = reservationTimeRepository.findReservedBy(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return ReservationStatus.of(reservedTimes, reservationTimes);
    }
}
