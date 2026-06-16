package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.PaymentAmountMismatchException;
import roomescape.global.exception.RoomEscapeException;
import roomescape.global.exception.UniqueConstraintViolationException;
import roomescape.reservation.application.dto.PaymentConfirmCommand;
import roomescape.reservation.application.port.out.payment.PaymentConfirmation;
import roomescape.reservation.application.port.out.payment.PaymentGateway;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.application.port.out.payment.PaymentStatus;
import roomescape.reservation.domain.PaymentOrder;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.PaymentOrderRepository;
import roomescape.reservation.domain.repository.ReservationRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class PaymentService {

    private static final Long DEFAULT_AMOUNT = 50_000L;

    private final PaymentOrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    public PaymentOrder prepare(Long reservationId) {
        try {
            PaymentOrder order = PaymentOrder.create(reservationId, DEFAULT_AMOUNT);
            return orderRepository.save(order);
        } catch (UniqueConstraintViolationException e) {
            throw new ConflictException("결제 주문 생성에 실패했습니다. 다시 시도해주세요.");
        }
    }

    public PaymentResult confirm(PaymentConfirmCommand command) {
        PaymentOrder order = orderRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 결제 주문입니다."));

        validateAmount(order, command.amount());
        validatePending(order);

        PaymentResult result = paymentGateway.confirm(
                new PaymentConfirmation(command.paymentKey(), command.orderId(), command.amount()));
        validateApproved(result);
        confirmPaymentOrder(order.confirm(command.paymentKey()));
        confirmReservation(order);

        return result;
    }

    private void validateAmount(PaymentOrder order, Long amount) {
        Long expectedAmount = order.getAmount().value();
        if (!expectedAmount.equals(amount)) {
            throw new PaymentAmountMismatchException(expectedAmount, amount);
        }
    }

    private void validatePending(PaymentOrder order) {
        if (!order.isPending()) {
            throw new ConflictException("이미 처리된 결제입니다.");
        }
    }

    private void validateApproved(PaymentResult result) {
        if (result.status() != PaymentStatus.DONE) {
            throw new RoomEscapeException("결제가 완료되지 않았습니다.");
        }
    }

    private void confirmPaymentOrder(PaymentOrder order) {
        try {
            if (orderRepository.confirm(order) == 0) {
                throw new ConflictException("이미 처리된 결제입니다.");
            }
        } catch (UniqueConstraintViolationException e) {
            throw new ConflictException("이미 처리된 결제입니다.");
        }
    }

    private void confirmReservation(PaymentOrder order) {
        if (reservationRepository.updateStatus(order.getReservationId(), ReservationStatus.CONFIRMED) == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }
    }
}
