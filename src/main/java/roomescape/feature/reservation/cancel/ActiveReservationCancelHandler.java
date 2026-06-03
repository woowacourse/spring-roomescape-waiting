package roomescape.feature.reservation.cancel;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.repository.ReservationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveReservationCancelHandler {

    private final ReservationRepository reservationRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmFastestWaiting(ActiveReservationCancelEvent event) {
        try {
            promoteFastestWaiting(event);
        } catch (Exception exception) {
            log.error(
                    "대기 예약 자동 승격에 실패했습니다. date={}, timeId={}, themeId={}",
                    event.date(), event.timeId(), event.themeId(), exception
            );
        }
    }

    private void promoteFastestWaiting(ActiveReservationCancelEvent event) {
        Optional<Reservation> candidate = reservationRepository.findLowestIdWaitingReservation(
                event.date(), event.timeId(), event.themeId());
        if (candidate.isEmpty()) {
            return;
        }

        int changedRowCount = reservationRepository.changeStatus(
                candidate.get().getId(), ReservationStatus.WAITING, ReservationStatus.ACTIVE);

        if (changedRowCount <= 0) {
            // 동시성으로 인해 승격할 예약을 찾지 못했다면 제시도
            promoteFastestWaiting(event);
        }
    }
}
