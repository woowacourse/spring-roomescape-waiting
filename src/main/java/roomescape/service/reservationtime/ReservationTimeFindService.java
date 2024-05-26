package roomescape.service.reservationtime;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTimeStatuses;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeFindService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeFindService(ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTime> findReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    public ReservationTimeStatuses findReservationStatuses(LocalDate date, long themeId) {
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, themeId);
        List<ReservationTime> reservedTimes = reservations.stream()
                .map(Reservation::getReservationTime)
                .toList();
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return ReservationTimeStatuses.of(reservedTimes, reservationTimes);
    }
}
