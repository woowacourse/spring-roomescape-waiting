package roomescape.feature.reservation.cancel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.repository.ReservationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCancelHandler {

    private final ReservationRepository reservationRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmFastestWaiting(ReservationCancelEvent event) {
        try {
            reservationRepository.findLowestIdWaitingReservation(
                            event.date(),
                            event.timeId(),
                            event.themeId()
                    ).map(Reservation::confirmWaiting)
                    .ifPresent(reservationRepository::update);
        } catch (Exception exception) {
            log.error(
                    "대기 예약 자동 승격에 실패했습니다. date={}, timeId={}, themeId={}",
                    event.date(), event.timeId(), event.themeId(), exception
            );
        }
    }
}
