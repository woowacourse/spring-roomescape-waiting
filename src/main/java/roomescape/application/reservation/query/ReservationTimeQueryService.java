package roomescape.application.reservation.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.query.dto.AvailableReservationTimeResult;
import roomescape.application.reservation.query.dto.ReservationTimeResult;
import roomescape.domain.reservation.DailyThemeReservations;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeQueryService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeQueryService(ReservationTimeRepository reservationTimeRepository,
                                       ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResult> findAll() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResult::from)
                .toList();
    }

    public List<AvailableReservationTimeResult> findAvailableTimesByThemeIdAndDate(Long themeId,
                                                                                   LocalDate reservationDate) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Reservation> reservations = reservationRepository.findByThemeIdAndDate(
                themeId,
                reservationDate
        );
        DailyThemeReservations dailyThemeReservations = new DailyThemeReservations(
                reservations,
                themeId,
                reservationDate
        );
        Set<ReservationTime> bookedTimes = dailyThemeReservations.calculateBookedTimes();
        return reservationTimes.stream()
                .map(reservationTime ->
                        new AvailableReservationTimeResult(
                                reservationTime.getId(),
                                reservationTime.getStartAt(),
                                bookedTimes.contains(reservationTime)
                        )
                )
                .toList();
    }
}
