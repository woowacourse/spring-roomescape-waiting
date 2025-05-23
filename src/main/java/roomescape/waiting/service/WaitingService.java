package roomescape.waiting.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.web.exception.NotAuthorizationException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationStatusRepository;
import roomescape.reservation.service.manager.ReservationManager;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private static final ReservationStatus WAITING = ReservationStatus.WAITING;

    private final ReservationStatusRepository statusRepository;
    private final ReservationManager reservationManager;
    private final WaitingQueryService waitingQueryService;

    @Transactional
    public void promoteFirstWaitingToReservation(LocalDate date, Long timeId) {
        if (statusRepository.existsByDateAndTimeIdAndStatus(date, timeId, WAITING)) {
            Reservation waiting = statusRepository.findByDateAndTimeIdAndStatus(date, timeId, WAITING).getFirst();
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
