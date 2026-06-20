package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.ServiceTest;
import roomescape.dao.ReservationDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.code.PaymentErrorCode;
import roomescape.exception.domain.PaymentException;

class PaymentServiceTest extends ServiceTest {

    private static final long DEFAULT_AMOUNT = 10_000L;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationDao reservationDao;

    @Test
    void 결제_승인_성공_시_예약이_CONFIRMED_상태로_변경된다() {
        // given
        ReservationTime time = fixtureGenerator.saveReservationTime(LocalTime.of(13, 0));
        Theme theme = fixtureGenerator.saveTheme("테마1", "설명", "https://img");
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 10, 0);

        ReservationResponse created = reservationService.create(
                new ReservationRequest("러키", LocalDate.of(2026, 7, 1), time.getId(), theme.getId(), DEFAULT_AMOUNT),
                now
        );
        String orderId = created.orderId();

        given(paymentGateway.confirm(any())).willReturn(new PaymentResult("test-payment-key"));

        // when
        paymentService.confirm("test-payment-key", orderId, DEFAULT_AMOUNT);

        // then
        Reservation reservation = reservationDao.findById(created.id()).orElseThrow();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void 조작된_amount는_승인_전에_차단되고_게이트웨이가_호출되지_않는다() {
        // given
        ReservationTime time = fixtureGenerator.saveReservationTime(LocalTime.of(13, 0));
        Theme theme = fixtureGenerator.saveTheme("테마1", "설명", "https://img");
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 10, 0);

        ReservationResponse created = reservationService.create(
                new ReservationRequest("러키", LocalDate.of(2026, 7, 1), time.getId(), theme.getId(), DEFAULT_AMOUNT),
                now
        );
        String orderId = created.orderId();
        long manipulatedAmount = DEFAULT_AMOUNT + 1;

        // when & then
        assertThatThrownBy(() -> paymentService.confirm("test-payment-key", orderId, manipulatedAmount))
                .isInstanceOf(PaymentException.class)
                .satisfies(ex -> assertThat(((PaymentException) ex).getExceptionCode())
                        .isEqualTo(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH));

        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
    }
}
