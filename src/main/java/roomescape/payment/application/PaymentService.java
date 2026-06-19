package roomescape.payment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.payment.application.dto.request.PaymentConfirmRequest;
import roomescape.payment.application.dto.request.PaymentFailRequest;
import roomescape.payment.application.dto.response.PaymentConfirmResponse;
import roomescape.payment.application.port.in.ConfirmPaymentUseCase;
import roomescape.payment.application.port.in.HandlePaymentFailureUseCase;
import roomescape.payment.application.port.out.PaymentGateway;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

@Service
@RequiredArgsConstructor
public class PaymentService implements ConfirmPaymentUseCase, HandlePaymentFailureUseCase {

    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    @Override
    @Transactional
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request, long memberId) {
        Reservation reservation = findReservationByOrderId(request.orderId());
        reservation.validateOwnedBy(memberId);
        validateAmount(reservation, request.amount());

        if (reservation.isConfirmedWith(request.paymentKey())) {
            return toConfirmResponse(reservation);
        }
        validatePending(reservation);

        PaymentResult result = paymentGateway.confirm(
                new PaymentConfirmation(request.paymentKey(), request.orderId(), request.amount())
        );
        validateGatewayResult(request, result);

        boolean confirmed = reservationRepository.confirmPayment(
                reservation.getId(),
                reservation.getOrderId(),
                request.paymentKey()
        );
        if (!confirmed) {
            throw new EscapeRoomException(ErrorCode.PAYMENT_NOT_PENDING, reservation.getOrderId());
        }

        return new PaymentConfirmResponse(
                reservation.getId(),
                reservation.getOrderId(),
                request.paymentKey(),
                reservation.getAmount(),
                ReservationStatus.CONFIRMED
        );
    }

    private Reservation findReservationByOrderId(String orderId) {
        return reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.PAYMENT_NOT_FOUND, orderId));
    }

    private void validateAmount(Reservation reservation, int requestedAmount) {
        if (reservation.getAmount() != requestedAmount) {
            throw new EscapeRoomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH, reservation.getAmount(), requestedAmount);
        }
    }

    private void validatePending(Reservation reservation) {
        if (!reservation.isPending()) {
            throw new EscapeRoomException(ErrorCode.PAYMENT_NOT_PENDING, reservation.getOrderId());
        }
    }

    private void validateGatewayResult(PaymentConfirmRequest request, PaymentResult result) {
        boolean matchesRequest = request.paymentKey().equals(result.paymentKey())
                && request.orderId().equals(result.orderId())
                && request.amount() == result.totalAmount()
                && "DONE".equals(result.status());

        if (!matchesRequest) {
            throw new EscapeRoomException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    private PaymentConfirmResponse toConfirmResponse(Reservation reservation) {
        return new PaymentConfirmResponse(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getPaymentKey(),
                reservation.getAmount(),
                reservation.getStatus()
        );
    }

    @Override
    @Transactional
    public void handleFailure(PaymentFailRequest request, long memberId) {
        if (!StringUtils.hasText(request.orderId())) {
            return;
        }
        reservationRepository.deletePendingByOrderIdAndMemberId(request.orderId(), memberId);
    }
}
