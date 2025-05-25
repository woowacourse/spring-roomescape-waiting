package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.web.exception.NotAuthorizationException;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.service.WaitingService;

@Service
@RequiredArgsConstructor
public class ReservedService {

    private final ReservedQueryService reservedQueryService;
    private final WaitingService waitingService;

    @Transactional
    public void cancel(Long id, Long userId) {
        Reservation reserved = reservedQueryService.getReserved(id);

        if (!reserved.isOwner(userId)) {
            throw new NotAuthorizationException("해당 예약자가 아닙니다.");
        }

        reserved.cancelReservation();
        waitingService.promoteFirstWaitingToReservation(reserved.getDate(), reserved.getTimeId());
    }
}
