package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Payment;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.exception.PaymentAmountMismatchException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String PAYMENT_KEY = "test_payment_key";
    private static final String ORDER_ID = "order-abc123";
    private static final Long RESERVATION_ID = 1L;
    private static final Long AMOUNT = 30_000L;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("주문 생성 시 payment_key 가 없는 주문을 저장한다")
    void createOrder() {
        given(paymentRepository.save(any(Payment.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Payment order = paymentService.createOrder(RESERVATION_ID, AMOUNT);

        assertThat(order.reservationId()).isEqualTo(RESERVATION_ID);
        assertThat(order.amount()).isEqualTo(AMOUNT);
        assertThat(order.orderId()).startsWith("order-");
        assertThat(order.paymentKey()).isNull();
    }

    @Test
    @DisplayName("금액이 일치하면 승인 후 payment_key 저장과 예약 확정이 일어난다")
    void confirm() {
        Payment order = new Payment(10L, RESERVATION_ID, ORDER_ID, AMOUNT, null);
        PaymentResult result = new PaymentResult(PAYMENT_KEY, ORDER_ID, PaymentStatus.DONE, AMOUNT);
        given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(order);
        given(paymentGateway.confirm(any(PaymentConfirmation.class))).willReturn(result);

        PaymentResult actual = paymentService.confirm(PAYMENT_KEY, ORDER_ID, AMOUNT);

        assertThat(actual).isEqualTo(result);
        verify(paymentRepository).updatePaymentKey(ORDER_ID, PAYMENT_KEY);
        verify(reservationRepository).confirm(RESERVATION_ID);
    }

    @Test
    @DisplayName("금액이 다르면 게이트웨이 호출 전에 PaymentAmountMismatchException 으로 차단한다")
    void confirm_amountMismatch() {
        Payment order = new Payment(10L, RESERVATION_ID, ORDER_ID, AMOUNT, null);
        given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(order);

        assertThatThrownBy(() -> paymentService.confirm(PAYMENT_KEY, ORDER_ID, 9_999L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verifyNoInteractions(paymentGateway);
        verify(paymentRepository, never()).updatePaymentKey(any(), any());
        verify(reservationRepository, never()).confirm(any());
    }
}
