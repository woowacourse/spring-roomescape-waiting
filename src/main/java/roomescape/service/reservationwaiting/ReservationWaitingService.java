package roomescape.service.reservationwaiting;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.exception.ErrorCode;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;

    public ReservationWaitingService(
            final ReservationRepository reservationRepository,
            final ReservationWaitingRepository reservationWaitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    public ReservationWaiting save(String name, LocalDate date, Long themeId, Long timeId) {
        Long reservationId = findReservationId(date, themeId, timeId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보가 없으면 대기 생성이 불가능합니다."
                ));

        ReservationWaiting nonIdReservationWaiting = ReservationWaiting.createNew(reservation, name, LocalTime.now());
        return reservationWaitingRepository.save(nonIdReservationWaiting);
    }

    private Long findReservationId(final LocalDate date, final Long themeId, final Long timeId) {
        try {
            return reservationRepository.findReservationIdByDateAndThemeIdAndTimeId(date, themeId, timeId);
        } catch (EmptyResultDataAccessException exception) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESERVATION_NOT_FOUND,
                    "예약 정보가 없으면 대기 생성이 불가능합니다."
            );
        }
    }
}
