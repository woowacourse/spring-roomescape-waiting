package roomescape.reservationwaiting.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.Reservation;
import roomescape.reservationwaiting.ReservationWaiting;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(
            final ReservationWaitingRepository reservationWaitingRepository
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    public ReservationWaiting save(String name, Reservation reservation) {

        validateWaitableName(reservation, name);

        ReservationWaiting nonIdReservationWaiting = ReservationWaiting.createNew(reservation, name, LocalDateTime.now());
        return reservationWaitingRepository.save(nonIdReservationWaiting);
    }

    private void validateWaitableName(final Reservation reservation, final String waitingName) {
        if (reservation.getName().equals(waitingName)) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 예약한 사람은 같은 예약에 대기할 수 없습니다."
            );
        }

        if (reservationWaitingRepository.existsByReservationIdAndName(reservation.getId(), waitingName)) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 같은 예약에 대기 중입니다."
            );
        }
    }

    public void deleteByIdAndName(Long waitingId, String name) {
        int affectedRowCount = reservationWaitingRepository.deleteByIdAndName(waitingId, name);

        if (affectedRowCount <= 0) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                    "본인의 대기만 취소할 수 있습니다.");
        }
    }
}
