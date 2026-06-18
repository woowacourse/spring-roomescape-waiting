package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.PaymentResult;

class PaymentReservationServiceTest {

    private ReservationService reservationService;
    private PaymentReservationService paymentReservationService;
    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void beforeEach() {
        reservationService = Mockito.mock(ReservationService.class);
        paymentReservationService = new PaymentReservationService(reservationService);
        reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");
    }

    @Test
    void preparePaymentConfirmationLocksAndValidatesPaymentTest() {
        Reservation pendingReservation = pendingReservation();
        when(reservationService.findByOrderId("order_test")).thenReturn(pendingReservation);

        Reservation result = paymentReservationService.preparePaymentConfirmation("order_test", 50000L);

        assertThat(result).isEqualTo(pendingReservation);
        verify(reservationService, times(1)).lockByOrderId("order_test");
        verify(reservationService, times(1)).findByOrderId("order_test");
    }

    @Test
    void preparePaymentConfirmationAmountMismatchExceptionTest() {
        when(reservationService.findByOrderId("order_test")).thenReturn(pendingReservation());

        assertThatThrownBy(() -> paymentReservationService.preparePaymentConfirmation("order_test", 1000L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code())
                        .isEqualTo(DomainErrorCode.PAYMENT_AMOUNT_MISMATCH));
    }

    @Test
    void confirmPaymentValidatesResultAndConfirmsReservationTest() {
        PaymentResult paymentResult = new PaymentResult("payment_key", "order_test", "DONE", 50000L);
        Reservation confirmedReservation = pendingReservation().confirmPayment("payment_key");
        when(reservationService.confirmPayment("order_test", "payment_key")).thenReturn(confirmedReservation);

        Reservation result = paymentReservationService.confirmPayment("order_test", paymentResult, 50000L);

        assertThat(result).isEqualTo(confirmedReservation);
        verify(reservationService, times(1)).confirmPayment("order_test", "payment_key");
    }

    @Test
    void confirmPaymentGatewayResultMismatchExceptionTest() {
        PaymentResult paymentResult = new PaymentResult("payment_key", "other_order", "DONE", 50000L);

        assertThatThrownBy(() -> paymentReservationService.confirmPayment("order_test", paymentResult, 50000L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code())
                        .isEqualTo(DomainErrorCode.PAYMENT_FAILED));
        verify(reservationService, never()).confirmPayment("order_test", "payment_key");
    }

    @Test
    void markPaymentUnknownTest() {
        Reservation unknownReservation = pendingReservation().markPaymentUnknown();
        when(reservationService.markPaymentUnknown("order_test")).thenReturn(unknownReservation);

        Reservation result = paymentReservationService.markPaymentUnknown("order_test");

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PAYMENT_UNKNOWN);
        verify(reservationService, times(1)).markPaymentUnknown("order_test");
    }

    private Reservation pendingReservation() {
        return new Reservation(1L, "fizz", LocalDate.of(2026, 5, 3), reservationTime, theme,
                ReservationStatus.PENDING, "order_test", 50000L, null);
    }
}
