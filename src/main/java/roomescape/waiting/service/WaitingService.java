package roomescape.waiting.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.web.exception.NotAuthorizationException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.manager.ReservationManager;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final ReservationManager reservationManager;
    private final WaitingQueryService waitingQueryService;

    @Transactional
    public void promoteFirstWaitingToReservation(LocalDate date, Long timeId) {
        if (waitingQueryService.existsByDateAndTimeId(date, timeId)) {
            Reservation waiting = waitingQueryService.getFirstByDateAndTimeId(date, timeId);
            waiting.reserved();
        }
    }

    @Transactional
    public void cancelWaiting(Long id, Long memberId) {
        Reservation waiting = waitingQueryService.getWaiting(id);
        if (waiting.isOwner(memberId)) {
            waiting.cancelWaiting();
            return;
        }

        throw new NotAuthorizationException("해당 예약 대기자가 아닙니다.");
    }

    @Transactional
    public void delete(Long id) {
        Reservation waiting = waitingQueryService.getWaiting(id);
        reservationManager.delete(waiting);
    }
}
