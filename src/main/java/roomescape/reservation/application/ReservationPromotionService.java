package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    public void cancelReservationAndPromoteFirstWaiting(long reservationId, long scheduleId) {
        Waiting firstWaiting = waitingRepository.findFirstByScheduleIdForUpdate(scheduleId)
                .orElse(null);
        if (firstWaiting != null) {
            waitingRepository.deleteById(firstWaiting.getId());
            reservationRepository.deleteById(reservationId);
            reservationRepository.save(new Reservation(null, firstWaiting.getMemberId(), scheduleId));
            return;
        }
        reservationRepository.deleteById(reservationId);
    }
}
