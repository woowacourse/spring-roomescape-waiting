package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import roomescape.domain.Payment;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentStatus;
import roomescape.domain.repository.PaymentOrderRepository;
import roomescape.domain.repository.PaymentRepository;
import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.domain.vo.PaymentConfirmation;
import roomescape.domain.vo.PaymentResult;
import roomescape.dto.PaymentConfirmRequest;
import roomescape.dto.PaymentFailRequest;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.infrastructure.toss.TossPaymentException;
import roomescape.service.port.PaymentGateway;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String ORDER_ID = "order-123";
    private static final String PAYMENT_KEY = "payment-key";
    private static final String IDEMPOTENCY_KEY = ORDER_ID;

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationSlotRepository reservationSlotRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 승인 전 저장된 주문 금액과 요청 금액이 다르면 게이트웨이를 호출하지 않는다")
    void confirm_amountMismatch_doesNotCallGateway() {
        PaymentOrder paymentOrder = new PaymentOrder(1L, 10L, ORDER_ID, 10_000L);
        when(paymentOrderRepository.getByOrderId(ORDER_ID)).thenReturn(paymentOrder);

        PaymentConfirmRequest request = new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 1_000L);

        assertThatThrownBy(() -> paymentService.confirm(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PAYMENT_AMOUNT_MISMATCH.getMessage());

        verifyNoInteractions(paymentGateway);
        verify(paymentRepository, never()).save(any());
        verify(reservationSlotRepository, never()).confirmPayment(any(Long.class));
    }

    @Test
    @DisplayName("결제 승인이 성공하면 결제 정보를 저장하고 예약을 확정한다")
    void confirm_success_savesPaymentAndConfirmsReservation() {
        PaymentOrder paymentOrder = new PaymentOrder(1L, 10L, ORDER_ID, 10_000L);
        when(paymentOrderRepository.getByOrderId(ORDER_ID)).thenReturn(paymentOrder);
        when(paymentGateway.confirm(paymentConfirmation()))
                .thenReturn(new PaymentResult(PAYMENT_KEY, ORDER_ID, 10_000L));

        paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment payment = paymentCaptor.getValue();
        assertThat(payment.getPaymentOrderId()).isEqualTo(1L);
        assertThat(payment.getPaymentKey()).isEqualTo(PAYMENT_KEY);
        assertThat(payment.getAmount()).isEqualTo(10_000L);
        verify(paymentOrderRepository).updateStatus(1L, PaymentStatus.CONFIRMED);
        verify(reservationSlotRepository).confirmPayment(10L);
    }

    @Test
    @DisplayName("결제 승인 요청은 저장된 주문 번호를 멱등키로 사용한다")
    void confirm_usesStoredOrderIdAsIdempotencyKey() {
        PaymentOrder paymentOrder = new PaymentOrder(1L, 10L, ORDER_ID, 10_000L);
        when(paymentOrderRepository.getByOrderId(ORDER_ID)).thenReturn(paymentOrder);
        when(paymentGateway.confirm(any()))
                .thenReturn(new PaymentResult(PAYMENT_KEY, ORDER_ID, 10_000L));

        paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L));

        ArgumentCaptor<PaymentConfirmation> confirmationCaptor = ArgumentCaptor.forClass(PaymentConfirmation.class);
        verify(paymentGateway).confirm(confirmationCaptor.capture());
        PaymentConfirmation confirmation = confirmationCaptor.getValue();
        assertThat(confirmation.orderId()).isEqualTo(paymentOrder.getOrderId());
        assertThat(confirmation.idempotencyKey()).isEqualTo(paymentOrder.getIdempotencyKey());
        assertThat(confirmation.idempotencyKey()).hasSizeLessThanOrEqualTo(PaymentOrder.MAX_IDEMPOTENCY_KEY_LENGTH);
    }

    @Test
    @DisplayName("타임아웃 후 같은 주문을 다시 승인해도 같은 멱등키로 토스를 호출한다")
    void confirm_retryAfterTimeout_usesSameIdempotencyKey() {
        PaymentOrder paymentOrder = new PaymentOrder(1L, 10L, ORDER_ID, 10_000L);
        when(paymentOrderRepository.getByOrderId(ORDER_ID)).thenReturn(paymentOrder);
        when(paymentGateway.confirm(any()))
                .thenThrow(new RestClientException("Read timed out", new SocketTimeoutException("Read timed out")))
                .thenReturn(new PaymentResult(PAYMENT_KEY, ORDER_ID, 10_000L));

        assertThatExceptionOfType(CustomException.class)
                .isThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L)))
                .satisfies(exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.PAYMENT_CONFIRM_RESULT_UNKNOWN));
        verify(paymentOrderRepository).updateStatus(1L, PaymentStatus.UNKNOWN);
        paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L));

        ArgumentCaptor<PaymentConfirmation> confirmationCaptor = ArgumentCaptor.forClass(PaymentConfirmation.class);
        verify(paymentGateway, times(2)).confirm(confirmationCaptor.capture());
        List<PaymentConfirmation> confirmations = confirmationCaptor.getAllValues();
        assertThat(confirmations)
                .extracting(PaymentConfirmation::idempotencyKey)
                .containsExactly(IDEMPOTENCY_KEY, IDEMPOTENCY_KEY);
    }

    @Test
    @DisplayName("success 페이지 새로고침으로 같은 주문 승인 요청이 반복되어도 같은 멱등키로 토스를 호출한다")
    void confirm_refreshSuccessPage_usesSameIdempotencyKey() {
        PaymentOrder paymentOrder = new PaymentOrder(1L, 10L, ORDER_ID, 10_000L);
        when(paymentOrderRepository.getByOrderId(ORDER_ID)).thenReturn(paymentOrder);
        when(paymentGateway.confirm(any()))
                .thenReturn(new PaymentResult(PAYMENT_KEY, ORDER_ID, 10_000L));

        paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L));
        paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L));

        ArgumentCaptor<PaymentConfirmation> confirmationCaptor = ArgumentCaptor.forClass(PaymentConfirmation.class);
        verify(paymentGateway, times(2)).confirm(confirmationCaptor.capture());
        List<PaymentConfirmation> confirmations = confirmationCaptor.getAllValues();
        assertThat(confirmations)
                .extracting(PaymentConfirmation::idempotencyKey)
                .containsExactly(IDEMPOTENCY_KEY, IDEMPOTENCY_KEY);
    }

    @Test
    @DisplayName("토스가 에러 응답을 내려주면 결제 거절로 안내하고 예약을 확정하지 않는다")
    void confirm_tossError_distinguishesRejection() {
        PaymentOrder paymentOrder = new PaymentOrder(1L, 10L, ORDER_ID, 10_000L);
        when(paymentOrderRepository.getByOrderId(ORDER_ID)).thenReturn(paymentOrder);
        when(paymentGateway.confirm(paymentConfirmation()))
                .thenThrow(new TossPaymentException.CardRejected("카드 결제가 거절되었습니다."));

        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L)))
                .isInstanceOf(TossPaymentException.CardRejected.class)
                .hasMessage("카드 결제가 거절되었습니다.");

        verify(paymentRepository, never()).save(any());
        verify(reservationSlotRepository, never()).confirmPayment(any(Long.class));
        verify(paymentOrderRepository).updateStatus(1L, PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("토스 연결 실패는 결제 거절과 구분해 연결 실패로 안내하고 예약을 확정하지 않는다")
    void confirm_connectionFailed_guidesConnectionProblem() {
        PaymentOrder paymentOrder = new PaymentOrder(1L, 10L, ORDER_ID, 10_000L);
        when(paymentOrderRepository.getByOrderId(ORDER_ID)).thenReturn(paymentOrder);
        when(paymentGateway.confirm(paymentConfirmation()))
                .thenThrow(new ResourceAccessException("I/O error", new ConnectException("Connection refused")));

        assertThatExceptionOfType(CustomException.class)
                .isThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L)))
                .satisfies(exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.PAYMENT_GATEWAY_CONNECTION_FAILED));

        verify(paymentRepository, never()).save(any());
        verify(reservationSlotRepository, never()).confirmPayment(any(Long.class));
        verify(paymentOrderRepository).updateStatus(1L, PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("토스 read timeout은 결제 실패로 단정하지 않고 결과 확인 필요로 안내하며 예약을 확정하지 않는다")
    void confirm_readTimeout_guidesUnknownResult() {
        PaymentOrder paymentOrder = new PaymentOrder(1L, 10L, ORDER_ID, 10_000L);
        when(paymentOrderRepository.getByOrderId(ORDER_ID)).thenReturn(paymentOrder);
        when(paymentGateway.confirm(paymentConfirmation()))
                .thenThrow(new RestClientException("Read timed out", new SocketTimeoutException("Read timed out")));

        assertThatExceptionOfType(CustomException.class)
                .isThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L)))
                .satisfies(exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_CONFIRM_RESULT_UNKNOWN);
                    assertThat(exception.getMessage()).contains("확인");
                    assertThat(exception.getMessage()).doesNotContain("실패");
                });

        verify(paymentRepository, never()).save(any());
        verify(reservationSlotRepository, never()).confirmPayment(any(Long.class));
        verify(paymentOrderRepository).updateStatus(1L, PaymentStatus.UNKNOWN);
    }

    @Test
    @DisplayName("ResourceAccessException으로 감싸진 read timeout도 결과 확인 필요로 안내한다")
    void confirm_resourceAccessReadTimeout_guidesUnknownResult() {
        PaymentOrder paymentOrder = new PaymentOrder(1L, 10L, ORDER_ID, 10_000L);
        when(paymentOrderRepository.getByOrderId(ORDER_ID)).thenReturn(paymentOrder);
        when(paymentGateway.confirm(paymentConfirmation()))
                .thenThrow(new ResourceAccessException("I/O error", new SocketTimeoutException("Read timed out")));

        assertThatExceptionOfType(CustomException.class)
                .isThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L)))
                .satisfies(exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_CONFIRM_RESULT_UNKNOWN);
                    assertThat(exception.getMessage()).doesNotContain("실패");
                });

        verify(paymentRepository, never()).save(any());
        verify(reservationSlotRepository, never()).confirmPayment(any(Long.class));
        verify(paymentOrderRepository).updateStatus(1L, PaymentStatus.UNKNOWN);
    }

    @Test
    @DisplayName("결제 실패 콜백에 orderId가 없어도 예외가 발생하지 않는다")
    void fail_withoutOrderId_doesNotThrow() {
        PaymentFailRequest request = new PaymentFailRequest("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.", null);

        assertThatCode(() -> paymentService.fail(request))
                .doesNotThrowAnyException();

        verifyNoInteractions(paymentOrderRepository, paymentRepository, reservationSlotRepository, paymentGateway);
    }

    private PaymentConfirmation paymentConfirmation() {
        return new PaymentConfirmation(PAYMENT_KEY, ORDER_ID, 10_000L, IDEMPOTENCY_KEY);
    }
}
