package roomescape.payment;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationService;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentRepository;

@Component
public class CancelRetryJob {

    private static final Logger log = LoggerFactory.getLogger(CancelRetryJob.class);
    private static final long RETRY_INTERVAL_MS = 60_000L;

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final ReservationService reservationService;
    private final int maxAttempts;

    public CancelRetryJob(
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            ReservationService reservationService,
            @Value("${cancel-retry.max-attempts:5}") int maxAttempts
    ) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.reservationService = reservationService;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelay = RETRY_INTERVAL_MS)
    public void retryPendingCancels() {
        List<Reservation> targets = reservationRepository.findAllByStatus(ReservationStatus.CANCEL_UNCERTAIN);
        if (targets.isEmpty()) {
            return;
        }
        log.info("CANCEL_UNCERTAIN 재시도 시작: {}건", targets.size());
        for (Reservation reservation : targets) {
            retryCancel(reservation);
        }
    }

    private void retryCancel(Reservation reservation) {
        Long reservationId = reservation.getId();
        paymentRepository.findByReservationId(reservationId).ifPresentOrElse(
                payment -> attemptCancel(reservationId, payment),
                () -> log.error("CANCEL_UNCERTAIN 상태이나 payment 레코드 없음 reservationId={}", reservationId)
        );
    }

    private void attemptCancel(Long reservationId, Payment payment) {
        if (payment.getCancelAttempts() >= maxAttempts) {
            reservationRepository.updateStatus(reservationId, ReservationStatus.CANCEL_FAILED);
            log.error("[운영 개입 필요] 취소 재시도 한도 초과 reservationId={} paymentKey={} attempts={}",
                    reservationId, payment.getPaymentKey(), payment.getCancelAttempts());
            return;
        }
        try {
            paymentGateway.cancel(payment.getPaymentKey(), "예약 확정 실패 재시도");
            reservationService.completeCancelAndCleanup(reservationId);
            log.info("CANCEL_UNCERTAIN 해소 완료 reservationId={} paymentKey={}", reservationId, payment.getPaymentKey());
        } catch (NetworkUncertain e) {
            paymentRepository.incrementCancelAttempts(reservationId);
            log.warn("CANCEL_UNCERTAIN 재시도 실패 ({}/{}), 다음 주기 재시도 reservationId={}",
                    payment.getCancelAttempts() + 1, maxAttempts, reservationId);
        } catch (PaymentGatewayException e) {
            paymentRepository.incrementCancelAttempts(reservationId);
            log.error("CANCEL_UNCERTAIN 재시도 중 처리 불가 오류 reservationId={} code={} message={}",
                    reservationId, e.getCode(), e.getMessage());
        }
    }
}
