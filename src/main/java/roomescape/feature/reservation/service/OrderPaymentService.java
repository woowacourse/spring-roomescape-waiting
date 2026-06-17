package roomescape.feature.reservation.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.feature.payment.domain.Payment;
import roomescape.feature.payment.dto.PaymentApproveRequest;
import roomescape.feature.payment.repository.PaymentRepository;
import roomescape.feature.reservation.domain.OrderStatus;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.global.error.exception.GeneralException;

/**
 * 결제 승인 결과를 예약 주문 상태와 결제 기록으로 영속화하는 책임을 가진다.
 *
 * PaymentApprover(외부 어댑터)와 분리되어 있으며, 외부 호출(approve)은 이 트랜잭션 밖에서 일어난다.
 * 같은 orderId 의 재요청은 orderId 멱등키(payment.order_id UNIQUE)와 find-or-create 로 한 건만 저장된다.
 * 동시성(중복 INSERT·낙관적 락 충돌)은 기존 컨벤션대로 재시도로 수렴시킨다.
 */
@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private static final int MAX_CONCURRENCY_ATTEMPTS = 2;
    private static final long CONCURRENCY_BACKOFF_MILLIS = 50L;
    private static final double CONCURRENCY_BACKOFF_MULTIPLIER = 2.0;

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    @Retryable(
            retryFor = {DuplicateKeyException.class, OptimisticLockingFailureException.class},
            maxAttempts = MAX_CONCURRENCY_ATTEMPTS,
            backoff = @Backoff(delay = CONCURRENCY_BACKOFF_MILLIS, multiplier = CONCURRENCY_BACKOFF_MULTIPLIER)
    )
    public void confirm(Long reservationId, PaymentApproveRequest request) {
        Reservation reservation = findReservation(reservationId);
        Reservation confirmed = reservation.confirmOrder(request.amount());

        reservationRepository.changeOrderStatus(
                reservationId, reservation.getVersion(), reservation.getOrderStatus(), confirmed.getOrderStatus());
        savePaymentIfAbsent(reservationId, request);
    }

    @Transactional
    @Retryable(
            retryFor = {DuplicateKeyException.class, OptimisticLockingFailureException.class},
            maxAttempts = MAX_CONCURRENCY_ATTEMPTS,
            backoff = @Backoff(delay = CONCURRENCY_BACKOFF_MILLIS, multiplier = CONCURRENCY_BACKOFF_MULTIPLIER)
    )
    public void markConfirmationRequired(Long reservationId, PaymentApproveRequest request) {
        Reservation reservation = findReservation(reservationId);
        if (reservation.getOrderStatus() == OrderStatus.CONFIRMED) {
            // 재확인이 이미 성공해 확정된 경우 — 확인 필요로 되돌리지 않는다.
            return;
        }
        if (reservation.getOrderStatus() == OrderStatus.PENDING) {
            Reservation marked = reservation.markConfirmationRequired();
            reservationRepository.changeOrderStatus(
                    reservationId, reservation.getVersion(), reservation.getOrderStatus(), marked.getOrderStatus());
        }
        savePaymentIfAbsent(reservationId, request);
    }

    @Transactional(readOnly = true)
    public Map<Long, Payment> findPaymentsByReservationIds(List<Long> reservationIds) {
        return paymentRepository.findByReservationIds(reservationIds).stream()
                .collect(Collectors.toMap(Payment::getReservationId, Function.identity(), (existing, ignored) -> existing));
    }

    private void savePaymentIfAbsent(Long reservationId, PaymentApproveRequest request) {
        if (paymentRepository.findByOrderId(request.orderId()).isPresent()) {
            return;
        }
        paymentRepository.save(
                Payment.create(reservationId, request.orderId(), request.paymentKey(), request.amount()));
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findReservationByIdAndNotDeleted(reservationId)
                .orElseThrow(() -> new GeneralException(ReservationErrorType.RESERVATION_NOT_FOUND));
    }
}
