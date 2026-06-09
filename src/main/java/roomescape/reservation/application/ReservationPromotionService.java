package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingRepository;

@Component
@RequiredArgsConstructor
public class ReservationPromotionService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    @Transactional
    public void cancelReservationAndPromoteFirstWaiting(Reservation reservation) {
        long scheduleId = reservation.getScheduleId();

        Waiting firstWaiting = popFirstWaiting(scheduleId);
        reservationRepository.deleteById(reservation.getId());
        promoteWaitingIfPresent(firstWaiting, scheduleId);
    }

    @Transactional
    public void changeReservationScheduleAndPromoteFirstWaiting(Reservation reservation, long newScheduleId) {
        long oldScheduleId = reservation.getScheduleId();

        Waiting firstWaiting = popFirstWaiting(oldScheduleId);
        updateReservationSchedule(reservation.getId(), newScheduleId);
        promoteWaitingIfPresent(firstWaiting, oldScheduleId);
    }

    private Waiting popFirstWaiting(long scheduleId) {
        Waiting firstWaiting = waitingRepository.findFirstByScheduleIdForPromotion(scheduleId)
                .orElse(null);
        if (firstWaiting != null) {
            waitingRepository.deleteById(firstWaiting.getId());
        }
        return firstWaiting;
    }

    private void promoteWaitingIfPresent(Waiting waiting, long oldScheduleId) {
        if (waiting != null) {
            reservationRepository.save(new Reservation(null, waiting.getMemberId(), oldScheduleId));
        }
    }

    private void updateReservationSchedule(long reservationId, long newScheduleId) {
        try {
            int affectedRow = reservationRepository.updateScheduleById(reservationId, newScheduleId);
            validateReservationUpdated(affectedRow);
        } catch (DuplicateKeyException e) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_ALREADY_EXIST);
        }
    }

    private void validateReservationUpdated(int affectedRow) {
        if (affectedRow != 1) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_UPDATE_FAILED);
        }
    }
}
