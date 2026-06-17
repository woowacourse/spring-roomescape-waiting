package roomescape.payment;

import java.util.UUID;
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
import roomescape.payment.PaymentGatewayException;
import roomescape.payment.client.TossPaymentGateway;
import roomescape.payment.dto.CheckoutResult;
import roomescape.payment.dto.PaymentConfirmResult;
import roomescape.payment.dto.PaymentResult;

@Service
public class PaymentService {

    private final TossPaymentGateway tossPaymentGateway;
    private final ReservationService reservationService;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public PaymentService(
            TossPaymentGateway tossPaymentGateway,
            ReservationService reservationService,
            ThemeRepository themeRepository,
            ReservationRepository reservationRepository
    ) {
        this.tossPaymentGateway = tossPaymentGateway;
        this.reservationService = reservationService;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public CheckoutResult checkout(ReservationRequest request, String themeName) {
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND));
        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
        reservationService.createPendingReservation(request, orderId);
        String orderName = themeName.isBlank() ? "방탈출 예약" : "방탈출 예약 — " + themeName;
        return new CheckoutResult(orderId, orderName, theme.getPrice());
    }

    public PaymentConfirmResult confirm(String paymentKey, String orderId, long amount) {
        Reservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        if (amount != reservation.getTheme().getPrice()) {
            throw new RoomescapeException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        PaymentResult paymentResult;
        try {
            paymentResult = tossPaymentGateway.confirm(paymentKey, orderId, amount);
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
            tossPaymentGateway.cancel(paymentKey, "예약 확정 실패");
            throw new RoomescapeException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }
}
