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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.exception.PaymentAmountMismatchException;
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

        given(paymentRepository.findByOrderId("order_1")).willReturn(Optional.of(payment));
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(paymentGateway.confirm(new PaymentConfirmation("payment-key", "order_1", 10000L)))
                .willReturn(new PaymentResult("payment-key", "order_1", PaymentStatus.DONE, 10000L));
        given(paymentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        PaymentResult result = paymentService.confirm("payment-key", "order_1", 10000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        then(paymentGateway).should().confirm(new PaymentConfirmation("payment-key", "order_1", 10000L));
        then(paymentRepository).should().save(any());
        then(reservationRepository).should().save(any());
    }

    private Reservation reservation() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationSlot slot = new ReservationSlot(LocalDate.now().plusDays(1), time, theme);
        return new Reservation(1L, "브라운", slot, LocalDateTime.now(), false);
    }
}
