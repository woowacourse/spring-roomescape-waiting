package roomescape.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.payment.application.PaymentService;
import roomescape.payment.application.dto.request.PaymentConfirmRequest;
import roomescape.payment.application.dto.request.PaymentFailRequest;
import roomescape.payment.application.dto.response.PaymentConfirmResponse;
import roomescape.payment.application.port.out.PaymentGateway;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.theme.domain.Theme;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String ORDER_ID = "order_123456";
    private static final String IDEMPOTENCY_KEY = "idempotency-key";
    private static final String PAYMENT_KEY = "payment-key";
    private static final int AMOUNT = 15_000;

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private PaymentGateway paymentGateway;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(reservationRepository, paymentGateway);
    }

    @Test
    @DisplayName("저장 금액과 요청 금액이 다르면 게이트웨이를 호출하기 전에 차단한다.")
    void amount_mismatch_blocks_before_gateway_call() {
        Reservation reservation = pendingReservation();
        when(reservationRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, AMOUNT + 1), 1L))
                .isInstanceOf(EscapeRoomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        verify(paymentGateway, never()).confirm(any());
        verify(reservationRepository, never()).confirmPayment(anyLong(), any(), any());
    }

    @Test
    @DisplayName("승인이 성공하면 예약을 확정하고 paymentKey를 저장한다.")
    void confirm_payment_successfully() {
        Reservation reservation = pendingReservation();
        when(reservationRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(reservation));
        when(paymentGateway.confirm(any())).thenReturn(new PaymentResult(PAYMENT_KEY, ORDER_ID, "DONE", AMOUNT));
        when(reservationRepository.confirmPayment(reservation.getId(), ORDER_ID, PAYMENT_KEY)).thenReturn(true);

        PaymentConfirmResponse response = paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, AMOUNT), 1L);

        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(response.paymentKey()).isEqualTo(PAYMENT_KEY);
        ArgumentCaptor<PaymentConfirmation> captor = ArgumentCaptor.forClass(PaymentConfirmation.class);
        verify(paymentGateway).confirm(captor.capture());
        assertThat(captor.getValue().idempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
        verify(reservationRepository).confirmPayment(reservation.getId(), ORDER_ID, PAYMENT_KEY);
    }

    @Test
    @DisplayName("read timeout처럼 결과가 불명확한 실패는 확인 필요 상태로 표시한다.")
    void timeout_unknown_marks_payment_check_required() {
        Reservation reservation = pendingReservation();
        when(reservationRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(reservation));
        when(paymentGateway.confirm(any()))
                .thenThrow(new EscapeRoomException(ErrorCode.PAYMENT_GATEWAY_TIMEOUT_UNKNOWN));

        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, AMOUNT), 1L))
                .isInstanceOf(EscapeRoomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_GATEWAY_TIMEOUT_UNKNOWN);
        verify(reservationRepository).markPaymentCheckRequired(reservation.getId(), ORDER_ID, PAYMENT_KEY);
        verify(reservationRepository, never()).confirmPayment(anyLong(), any(), any());
    }

    @Test
    @DisplayName("명확한 카드 거절은 결제 실패 상태로 표시한다.")
    void card_rejected_marks_payment_failed() {
        Reservation reservation = pendingReservation();
        when(reservationRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(reservation));
        when(paymentGateway.confirm(any()))
                .thenThrow(new EscapeRoomException(ErrorCode.PAYMENT_CARD_REJECTED));

        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, AMOUNT), 1L))
                .isInstanceOf(EscapeRoomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_CARD_REJECTED);
        verify(reservationRepository).markPaymentFailed(reservation.getId(), ORDER_ID, PAYMENT_KEY);
        verify(reservationRepository, never()).confirmPayment(anyLong(), any(), any());
    }

    @Test
    @DisplayName("PENDING 주문에서 이미 처리된 결제 응답은 안전하게 실패로 반환한다.")
    void already_processed_payment_for_pending_reservation_fails() {
        Reservation reservation = pendingReservation();
        when(reservationRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(reservation));
        when(paymentGateway.confirm(any()))
                .thenThrow(new EscapeRoomException(ErrorCode.PAYMENT_ALREADY_PROCESSED));

        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, AMOUNT), 1L))
                .isInstanceOf(EscapeRoomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        verify(reservationRepository, never()).confirmPayment(anyLong(), any(), any());
    }

    @Test
    @DisplayName("게이트웨이 승인 응답이 요청 값과 다르면 예약을 확정하지 않는다.")
    void gateway_result_mismatch_fails() {
        Reservation reservation = pendingReservation();
        when(reservationRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(reservation));
        when(paymentGateway.confirm(any())).thenReturn(new PaymentResult(PAYMENT_KEY, ORDER_ID, "ABORTED", AMOUNT));

        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, AMOUNT), 1L))
                .isInstanceOf(EscapeRoomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_GATEWAY_ERROR);
        verify(reservationRepository, never()).confirmPayment(anyLong(), any(), any());
    }

    @Test
    @DisplayName("failUrl의 orderId가 없으면 예약 정리 없이 정상 종료한다.")
    void payment_failure_without_order_id_is_ignored() {
        paymentService.handleFailure(new PaymentFailRequest("PAY_PROCESS_CANCELED", "사용자 취소", null), 1L);

        verify(reservationRepository, never()).markPendingPaymentFailedByOrderIdAndMemberId(any(), anyLong());
    }

    @Test
    @DisplayName("failUrl의 orderId가 있으면 결제 대기 주문을 실패 상태로 표시한다.")
    void payment_failure_with_order_id_marks_payment_failed() {
        paymentService.handleFailure(new PaymentFailRequest("PAY_PROCESS_ABORTED", "결제 실패", ORDER_ID), 1L);

        verify(reservationRepository).markPendingPaymentFailedByOrderIdAndMemberId(ORDER_ID, 1L);
    }

    private Reservation pendingReservation() {
        Theme theme = new Theme(1L, "theme", "description", "thumbnail", AMOUNT);
        Slot slot = Slot.of(
                1L,
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                theme,
                AMOUNT
        );
        return Reservation.of(1L, 1L, slot, ReservationStatus.PENDING, ORDER_ID, IDEMPOTENCY_KEY, AMOUNT, null);
    }
}
