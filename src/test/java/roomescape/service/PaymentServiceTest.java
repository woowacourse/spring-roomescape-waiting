package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import roomescape.domain.Payment;
import roomescape.domain.PaymentOrder;
import roomescape.domain.repository.PaymentOrderRepository;
import roomescape.domain.repository.PaymentRepository;
import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.domain.vo.PaymentConfirmation;
import roomescape.domain.vo.PaymentResult;
import roomescape.dto.PaymentConfirmRequest;
import roomescape.dto.PaymentFailRequest;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.service.port.PaymentGateway;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String ORDER_ID = "order-123";
    private static final String PAYMENT_KEY = "payment-key";

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
        when(paymentGateway.confirm(new PaymentConfirmation(PAYMENT_KEY, ORDER_ID, 10_000L)))
                .thenReturn(new PaymentResult(PAYMENT_KEY, ORDER_ID, 10_000L));

        paymentService.confirm(new PaymentConfirmRequest(PAYMENT_KEY, ORDER_ID, 10_000L));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment payment = paymentCaptor.getValue();
        assertThat(payment.getPaymentOrderId()).isEqualTo(1L);
        assertThat(payment.getPaymentKey()).isEqualTo(PAYMENT_KEY);
        assertThat(payment.getAmount()).isEqualTo(10_000L);
        verify(reservationSlotRepository).confirmPayment(10L);
    }

    @Test
    @DisplayName("결제 실패 콜백에 orderId가 없어도 예외가 발생하지 않는다")
    void fail_withoutOrderId_doesNotThrow() {
        PaymentFailRequest request = new PaymentFailRequest("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.", null);

        assertThatCode(() -> paymentService.fail(request))
                .doesNotThrowAnyException();

        verifyNoInteractions(paymentOrderRepository, paymentRepository, reservationSlotRepository, paymentGateway);
    }
}
