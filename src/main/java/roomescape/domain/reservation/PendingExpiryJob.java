package roomescape.domain.reservation;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PendingExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(PendingExpiryJob.class);
    private static final long CHECK_INTERVAL_MS = 60_000L;

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public PendingExpiryJob(ReservationRepository reservationRepository, ReservationService reservationService) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelay = CHECK_INTERVAL_MS)
    public void expireStalePayments() {
        List<Reservation> expired = reservationRepository.findExpiredPending(LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }
        log.info("결제 대기 TTL 만료 처리 시작: {}건", expired.size());
        for (Reservation reservation : expired) {
            try {
                reservationService.expireAndPromote(reservation.getId());
                log.info("결제 대기 만료 처리 완료 reservationId={} name={}", reservation.getId(), reservation.getName());
            } catch (Exception e) {
                log.error("결제 대기 만료 처리 실패 reservationId={}", reservation.getId(), e);
            }
        }
    }
}
