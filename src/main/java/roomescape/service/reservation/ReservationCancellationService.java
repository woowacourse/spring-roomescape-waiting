package roomescape.service.reservation;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.repository.PersistenceConflictException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;

@Service
public class ReservationCancellationService {
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationFactory reservationFactory;

    public ReservationCancellationService(
            final ReservationRepository reservationRepository,
            final ReservationWaitingRepository reservationWaitingRepository,
            final ReservationFactory reservationFactory
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationFactory = reservationFactory;
    }

    @Transactional
    public void cancel(final Reservation reservation, final LocalDateTime requestedAt) {
        reservationWaitingRepository.findFirstBySlot(reservation.getSlot())
                .ifPresentOrElse(
                        firstWaiting -> promoteFirstWaiting(reservation, firstWaiting, requestedAt),
                        () -> deleteReservation(reservation)
                );
    }

    private void promoteFirstWaiting(
            final Reservation reservation,
            final ReservationWaiting firstWaiting,
            final LocalDateTime requestedAt
    ) {
        deleteReservation(reservation);
        savePromotedReservation(firstWaiting, requestedAt);
        reservationWaitingRepository.delete(firstWaiting);
    }

    private void savePromotedReservation(final ReservationWaiting firstWaiting, final LocalDateTime requestedAt) {
        try {
            Reservation promotedReservation = reservationFactory.createNew(
                    firstWaiting.getName(),
                    firstWaiting.getReservation().getSlot(),
                    requestedAt
            );
            reservationRepository.save(promotedReservation);
        } catch (PersistenceConflictException exception) {
            throw new ConflictException(ErrorCode.RESERVATION_DUPLICATED, "동일한 시기에 예약을 할 수 없습니다.");
        }
    }

    private void deleteReservation(final Reservation reservation) {
        try {
            reservationRepository.delete(reservation);
        } catch (PersistenceConflictException exception) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_HAS_WAITING,
                    "예약 대기가 존재하는 예약은 바로 삭제할 수 없습니다."
            );
        }
    }
}
