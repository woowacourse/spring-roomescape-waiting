package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.exception.TossPaymentException;
import roomescape.repository.ReservationRepository;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @MockitoBean
    private ReservationRepository reservationRepository;

    @Test
    void 저장금액과_다른_amount면_확인전에_차단되고_게이트웨이는_호출되지_않는다() {
        Reservation reservation = new Reservation(1L, "테스터", LocalDate.now(),
                new ReservationTime(1L, LocalTime.of(9, 0)), new Theme(1L, "테마", "설명", "url"),
                ReservationStatus.PENDING_PAYMENT, "order-1", null, 10000L);
        given(reservationRepository.findByOrderId("order-1")).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> paymentService.confirm("test_pk_1", "order-1", 9000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 금액이_일치하면_게이트웨이를_호출한다() {
        Reservation reservation = new Reservation(1L, "테스터", LocalDate.now(),
                new ReservationTime(1L, LocalTime.of(9, 0)), new Theme(1L, "테마", "설명", "url"),
                ReservationStatus.PENDING_PAYMENT, "order-1", null, 10000L);
        given(reservationRepository.findByOrderId("order-1")).willReturn(Optional.of(reservation));

        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult("test_pk_1", "order-1", PaymentStatus.DONE, 10000L));

        var result = paymentService.confirm("test_pk_1", "order-1", 10000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        verify(paymentGateway).confirm(new PaymentConfirmation("test_pk_1", "order-1", 10000L));
    }

    @Test
    void ReadTimeout_발생시_UNCERTAIN_상태로_저장하고_예외를_던진다() {
        Reservation reservation = new Reservation(1L, "테스터", LocalDate.now(),
                new ReservationTime(1L, LocalTime.of(9, 0)), new Theme(1L, "테마", "설명", "url"),
                ReservationStatus.PENDING_PAYMENT, "order-1", null, 10000L);
        given(reservationRepository.findByOrderId("order-1")).willReturn(Optional.of(reservation));

        given(paymentGateway.confirm(any()))
                .willThrow(new TossPaymentException.ReadTimeout("타임아웃", new RuntimeException()));

        assertThatThrownBy(() -> paymentService.confirm("test_pk_1", "order-1", 10000L))
                .isInstanceOf(TossPaymentException.ReadTimeout.class);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.UNCERTAIN);
        verify(reservationRepository).updatePayment(1L, "test_pk_1", ReservationStatus.UNCERTAIN, "order-1", 10000L);
    }

}
