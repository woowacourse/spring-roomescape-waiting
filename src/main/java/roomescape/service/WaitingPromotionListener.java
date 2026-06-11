package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;
import roomescape.service.event.ReservationCancelledEvent;

@Component
public class WaitingPromotionListener {
    private static final Logger log = LoggerFactory.getLogger(WaitingPromotionListener.class);
    private final WaitingService waitingService;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public WaitingPromotionListener(
            WaitingService waitingService,
            ReservationRepository reservationRepository,
            Clock clock) {
        this.waitingService = waitingService;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void onReservationCancelled(ReservationCancelledEvent event) {
        waitingService.findFirstWaiting(event.getDate(), event.getTimeId(), event.getThemeId())
                .ifPresent(waiting -> {
                    waitingService.promoteWaiting(waiting);
                    reservationRepository.save(
                            Reservation.create(waiting.getName(), waiting.getDate(), waiting.getTime(),
                                    waiting.getTheme(), LocalDateTime.now(clock)));
                });
    }

    @Recover
    public void recover(Exception e, ReservationCancelledEvent event) {
        log.error("대기 승격 최종 실패 - reservationId: {}", event.getReservationId());
    }
}
