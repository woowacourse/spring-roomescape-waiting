package roomescape.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import roomescape.global.exception.BadRequestException;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentState;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.exception.PaymentAmountMismatchException;
import roomescape.payment.exception.PaymentConfirmationUncertainException;
import roomescape.payment.exception.PaymentConnectionFailedException;
import roomescape.payment.repository.PaymentRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentGateway paymentGateway;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, reservationRepository, paymentGateway, 50000L);
    }

    @Test
    @DisplayName("pending 결제를 생성하면 주문번호와 금액이 저장된다.")
    void issuePendingPayment_savesPayment() {
        Reservation reservation = reservation();
        given(paymentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.issuePendingPayment(reservation);

        assertThat(payment.getOrderId()).startsWith("order_");
        assertThat(payment.getAmount()).isEqualTo(50000L);
        then(paymentRepository).should().save(any());
    }

    @Test
    @DisplayName("저장된 금액과 요청 금액이 다르면 게이트웨이를 호출하지 않는다.")
    void confirm_amountMismatch_blocksGatewayCall() {
        Payment payment = Payment.pending(1L, "order_1", 10000L, LocalDateTime.now());
        given(paymentRepository.findByOrderId("order_1")).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order_1", 9000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        then(paymentGateway).should(never()).confirm(any());
    }

    @Test
    @DisplayName("금액이 일치하면 승인하고 예약을 확정한다.")
    void confirm_success_updatesReservationAndPayment() {
        Payment payment = Payment.pending(1L, "order_1", 10000L, LocalDateTime.now());
        Reservation reservation = reservation();
        PaymentConfirmation expectedConfirmation =
                new PaymentConfirmation("payment-key", "order_1", 10000L, payment.getIdempotencyKey());

        given(paymentRepository.findByOrderId("order_1")).willReturn(Optional.of(payment));
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(paymentGateway.confirm(expectedConfirmation))
                .willReturn(new PaymentResult("payment-key", "order_1", PaymentStatus.DONE, 10000L));
        given(paymentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        PaymentResult result = paymentService.confirm("payment-key", "order_1", 10000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        then(paymentGateway).should().confirm(expectedConfirmation);
        then(paymentRepository).should().save(any());
        then(reservationRepository).should().save(any());
    }

    @Test
    @DisplayName("연결 단계에서 실패하면 PaymentConnectionFailedException 으로 표면화되고 payment 상태는 그대로 둔다.")
    void confirm_resourceAccessException_throwsConnectionFailed() {
        Payment payment = Payment.pending(1L, "order_1", 10000L, LocalDateTime.now());
        given(paymentRepository.findByOrderId("order_1")).willReturn(Optional.of(payment));
        given(paymentGateway.confirm(any())).willThrow(new ResourceAccessException("connect timeout"));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order_1", 10000L))
                .isInstanceOf(PaymentConnectionFailedException.class);

        then(paymentRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("응답 읽기 단계에서 실패(read timeout)하면 PaymentConfirmationUncertainException 으로 표면화되고 payment 는 UNCERTAIN 으로 저장된다.")
    void confirm_restClientException_marksUncertain() {
        Payment payment = Payment.pending(1L, "order_1", 10000L, LocalDateTime.now());
        given(paymentRepository.findByOrderId("order_1")).willReturn(Optional.of(payment));
        given(paymentGateway.confirm(any())).willThrow(new RestClientException("read timeout"));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order_1", 10000L))
                .isInstanceOf(PaymentConfirmationUncertainException.class);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        then(paymentRepository).should().save(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo(PaymentState.UNCERTAIN);
        assertThat(captor.getValue().getPaymentKey()).isEqualTo("payment-key");
    }

    @Test
    @DisplayName("재시도할 결제 시도가 없으면(paymentKey 없음) BadRequestException 을 던진다.")
    void retryConfirmation_noAttemptedPaymentKey_throwsBadRequest() {
        Payment payment = Payment.pending(1L, "order_1", 10000L, LocalDateTime.now());
        given(paymentRepository.findByOrderId("order_1")).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.retryConfirmation("order_1"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("확인 필요 상태의 결제는 저장된 paymentKey 로 재시도해 승인한다.")
    void retryConfirmation_uncertainPayment_retriesWithStoredPaymentKey() {
        Payment payment = Payment.pending(1L, "order_1", 10000L, LocalDateTime.now())
                .markUncertain("payment-key", LocalDateTime.now());
        Reservation reservation = reservation();
        PaymentConfirmation expectedConfirmation =
                new PaymentConfirmation("payment-key", "order_1", 10000L, payment.getIdempotencyKey());

        given(paymentRepository.findByOrderId("order_1")).willReturn(Optional.of(payment));
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(paymentGateway.confirm(expectedConfirmation))
                .willReturn(new PaymentResult("payment-key", "order_1", PaymentStatus.DONE, 10000L));
        given(paymentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        PaymentResult result = paymentService.retryConfirmation("order_1");

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
    }

    private Reservation reservation() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationSlot slot = new ReservationSlot(LocalDate.now().plusDays(1), time, theme);
        return new Reservation(1L, "브라운", slot, LocalDateTime.now(), false);
    }
}
