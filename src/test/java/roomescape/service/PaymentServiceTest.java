package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import roomescape.domain.Payment;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.exception.PaymentAmountMismatchException;
import roomescape.service.exception.ResourceNotFoundException;

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
    @Mock
    private PlatformTransactionManager transactionManager;
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
    @DisplayName("같은 예약에 미승인 주문이 남아 있으면 새로 만들지 않고 재사용한다")
    void createOrder_reusesPendingOrder() {
        Payment existing = new Payment(10L, RESERVATION_ID, ORDER_ID, AMOUNT, null);
        given(paymentRepository.findByReservationId(RESERVATION_ID)).willReturn(Optional.of(existing));

        Payment order = paymentService.createOrder(RESERVATION_ID, AMOUNT);

        assertThat(order).isEqualTo(existing);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 승인된 주문만 있으면 새 주문을 생성한다")
    void createOrder_createsNewWhenExistingConfirmed() {
        Payment confirmed = new Payment(10L, RESERVATION_ID, ORDER_ID, AMOUNT, PAYMENT_KEY);
        given(paymentRepository.findByReservationId(RESERVATION_ID)).willReturn(Optional.of(confirmed));
        given(paymentRepository.save(any(Payment.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Payment order = paymentService.createOrder(RESERVATION_ID, AMOUNT);

        assertThat(order.paymentKey()).isNull();
        assertThat(order.orderId()).isNotEqualTo(ORDER_ID);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("금액이 일치하면 승인 후 payment_key 저장과 예약 확정이 일어난다")
    void confirm() {
        Payment order = new Payment(10L, RESERVATION_ID, ORDER_ID, AMOUNT, null);
        PaymentResult result = new PaymentResult(PAYMENT_KEY, ORDER_ID, PaymentStatus.DONE, AMOUNT);
        given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(order));
        given(paymentGateway.confirm(any(PaymentConfirmation.class))).willReturn(result);

        PaymentResult actual = paymentService.confirm(PAYMENT_KEY, ORDER_ID, AMOUNT);

        assertThat(actual).isEqualTo(result);
        verify(paymentRepository).updatePaymentKey(ORDER_ID, PAYMENT_KEY);
        verify(reservationRepository).confirm(RESERVATION_ID);
    }

    @Test
    @DisplayName("주문이 없으면 ResourceNotFoundException 으로 차단한다")
    void confirm_orderNotFound() {
        given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirm(PAYMENT_KEY, ORDER_ID, AMOUNT))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("금액이 다르면 게이트웨이 호출 전에 PaymentAmountMismatchException 으로 차단한다")
    void confirm_amountMismatch() {
        Payment order = new Payment(10L, RESERVATION_ID, ORDER_ID, AMOUNT, null);
        given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.confirm(PAYMENT_KEY, ORDER_ID, 9_999L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verifyNoInteractions(paymentGateway);
        verify(paymentRepository, never()).updatePaymentKey(any(), any());
        verify(reservationRepository, never()).confirm(any());
    }

    @Test
    @DisplayName("결제 실패 정리 시 PENDING 예약과 주문을 삭제한다")
    void cancelOrder_pending() {
        Payment order = new Payment(10L, RESERVATION_ID, ORDER_ID, AMOUNT, null);
        Reservation reservation = mock(Reservation.class);
        given(reservation.getStatus()).willReturn(ReservationStatus.PENDING);
        given(reservation.getId()).willReturn(RESERVATION_ID);
        given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(order));
        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(reservation));

        paymentService.cancelOrder(ORDER_ID);

        verify(reservationRepository).deleteById(RESERVATION_ID);
        verify(paymentRepository).deleteByOrderId(ORDER_ID);
    }

    @Test
    @DisplayName("결제 실패 정리 시 확정된 예약은 삭제하지 않는다")
    void cancelOrder_confirmed() {
        Payment order = new Payment(10L, RESERVATION_ID, ORDER_ID, AMOUNT, null);
        Reservation reservation = mock(Reservation.class);
        given(reservation.getStatus()).willReturn(ReservationStatus.CONFIRMED);
        given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(order));
        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(reservation));

        paymentService.cancelOrder(ORDER_ID);

        verify(reservationRepository, never()).deleteById(any());
        verify(paymentRepository, never()).deleteByOrderId(any());
    }
}
