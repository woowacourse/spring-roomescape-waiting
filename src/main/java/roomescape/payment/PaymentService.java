package roomescape.payment;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationService;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentGatewayException;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentRepository;
import roomescape.payment.dto.CheckoutResult;
import roomescape.payment.dto.PaymentConfirmResult;
import roomescape.payment.dto.PaymentResult;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String CANCEL_UNCERTAIN_STATUS = "CANCEL_UNCERTAIN";

    private final PaymentGateway paymentGateway;
    private final ReservationService reservationService;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(
            PaymentGateway paymentGateway,
            ReservationService reservationService,
            ThemeRepository themeRepository,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository
    ) {
        this.paymentGateway = paymentGateway;
        this.reservationService = reservationService;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
    }

    private void tryCancelWithFallback(String paymentKey, String orderId, long amount, Long reservationId) {
        try {
            paymentGateway.cancel(paymentKey, "예약 확정 실패");
        } catch (NetworkUncertain e) {
            log.error("보상 취소 타임아웃 — CANCEL_UNCERTAIN 상태로 전환 reservationId={} paymentKey={}", reservationId, paymentKey);
            paymentRepository.save(Payment.of(paymentKey, orderId, amount, CANCEL_UNCERTAIN_STATUS, reservationId));
            reservationRepository.updateStatus(reservationId, ReservationStatus.CANCEL_UNCERTAIN);
        }
    }

    public CheckoutResult checkout(ReservationRequest request, String themeName) {
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND));
        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
        reservationService.createPendingReservation(request, orderId, theme.getPrice());
        String orderName = themeName.isBlank() ? "방탈출 예약" : "방탈출 예약 — " + themeName;
        return new CheckoutResult(orderId, orderName, theme.getPrice());
    }

    public PaymentConfirmResult confirm(String paymentKey, String orderId, long amount) {
        Reservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        if (amount != reservation.getQuotedAmount()) {
            throw new RoomescapeException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        PaymentResult paymentResult;
        try {
            paymentResult = paymentGateway.confirm(paymentKey, orderId, amount);
        } catch (NetworkUncertain e) {
            reservationRepository.updateStatus(reservation.getId(), ReservationStatus.PAYMENT_UNCERTAIN);
            throw e;
        } catch (PaymentGatewayException e) {
            reservationService.deleteReservation(reservation.getId());
            throw e;
        }
        try {
            ReservationResponse result = reservationService.confirmPayment(orderId, paymentResult);
            return new PaymentConfirmResult(paymentResult, result);
        } catch (RoomescapeException e) {
            tryCancelWithFallback(paymentKey, orderId, amount, reservation.getId());
            throw new RoomescapeException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }
}
