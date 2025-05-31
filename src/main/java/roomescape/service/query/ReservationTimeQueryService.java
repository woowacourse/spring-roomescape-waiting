package roomescape.service.query;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.dto.time.AvailableReservationTimeResponseDto;
import roomescape.dto.time.ReservationTimeResponseDto;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeQueryService {

    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaReservationRepository reservationRepository;

    public ReservationTimeQueryService(final JpaReservationTimeRepository reservationTimeRepository,
                                       final JpaReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponseDto> findAllReservationTimes() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponseDto::new)
                .toList();
    }

    public List<AvailableReservationTimeResponseDto> findAllReservationTimesWithAvailabilityBy(
            LocalDate date, Long themeId
    ) {
        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAll();
        List<Reservation> bookedReservations = reservationRepository.findReservationsByDateAndThemeId(date, themeId);

        List<ReservationTime> alreadyBookedTimes = bookedReservations.stream()
                .map(Reservation::getTime)
                .toList();

        return allReservationTimes.stream()
                .map(reservationTime -> {
                    boolean alreadyBooked =  alreadyBookedTimes.contains(reservationTime);
                    return new AvailableReservationTimeResponseDto(reservationTime, alreadyBooked);
                })
                .toList();
    }
}
