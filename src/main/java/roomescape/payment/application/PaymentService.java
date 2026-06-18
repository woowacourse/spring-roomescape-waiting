package roomescape.payment.application;

import java.util.EnumSet;
import java.util.Set;
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

    private static final Set<ErrorCode> FAILED_PAYMENT_ERRORS = EnumSet.of(
            ErrorCode.PAYMENT_INVALID_REQUEST,
            ErrorCode.PAYMENT_CARD_REJECTED,
            ErrorCode.PAYMENT_NOT_FOUND,
            ErrorCode.PAYMENT_INVALID_IDEMPOTENCY_KEY
    );

    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    @Override
    @Transactional(noRollbackFor = EscapeRoomException.class)
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request, long memberId) {
        Reservation reservation = findReservationByOrderId(request.orderId());
        reservation.validateOwnedBy(memberId);
        validateAmount(reservation, request.amount());

        if (reservation.isConfirmedWith(request.paymentKey())) {
            return toConfirmResponse(reservation);
        }
        validatePending(reservation);

        PaymentResult result = confirmPayment(request, reservation);
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

    private PaymentResult confirmPayment(PaymentConfirmRequest request, Reservation reservation) {
        try {
            return paymentGateway.confirm(
                    new PaymentConfirmation(
                            request.paymentKey(),
                            request.orderId(),
                            reservation.getIdempotencyKey(),
                            request.amount()
                    )
            );
        } catch (EscapeRoomException exception) {
            handleGatewayException(request, reservation, exception);
            throw exception;
        }
    }

    private void handleGatewayException(
            PaymentConfirmRequest request,
            Reservation reservation,
            EscapeRoomException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();
        if (errorCode == ErrorCode.PAYMENT_GATEWAY_TIMEOUT_UNKNOWN) {
            reservationRepository.markPaymentCheckRequired(
                    reservation.getId(),
                    reservation.getOrderId(),
                    request.paymentKey()
            );
            return;
        }
        if (FAILED_PAYMENT_ERRORS.contains(errorCode)) {
            reservationRepository.markPaymentFailed(
                    reservation.getId(),
                    reservation.getOrderId(),
                    request.paymentKey()
            );
        }
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
        reservationRepository.markPendingPaymentFailedByOrderIdAndMemberId(request.orderId(), memberId);
    }
}
