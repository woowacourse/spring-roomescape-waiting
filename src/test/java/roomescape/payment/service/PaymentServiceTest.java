package roomescape.payment.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.payment.client.PaymentGateway;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.exception.PaymentAmountMismatchException;
import roomescape.payment.repository.PaymentRepository;
import roomescape.payment.service.dto.PaymentResult;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.slot.domain.ReservationSlot;
import roomescape.support.ServiceSupport;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Import({PaymentService.class})
class PaymentServiceTest extends ServiceSupport {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentGateway paymentGateway;

    private Payment savedPayment;
    private Reservation savedReservation;

    @BeforeEach
    void setUp() {
        ReservationDate date = saveDate(ReservationDateFixture.oneWeekLater());
        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        Theme theme = saveTheme("테마1");
        ReservationSlot slot = saveSlot(ReservationSlot.of(date, time, theme));
        savedReservation = reservationRepository.save(
                Reservation.reserve("테스터", slot.getId(), ReservationStatus.PENDING_PAYMENT, LocalDateTime.now())
        );
        savedPayment = paymentRepository.save(Payment.pending(savedReservation.getId(), slot.getId(), "test-order-id", 1000L));
    }

    @Test
    void 저장금액과_다른_amount면_확인전에_차단되고_게이트웨이는_호출되지_않는다() {
        assertThatThrownBy(() -> paymentService.confirm("test_pk", savedPayment.getOrderId(), 9000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 금액이_일치하면_게이트웨이를_호출하고_CONFIRMED로_저장된다() {
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult("test_pk", savedPayment.getOrderId(), roomescape.payment.service.dto.PaymentStatus.DONE, 1000L));

        PaymentResult result = paymentService.confirm("test_pk", savedPayment.getOrderId(), 1000L);

        Assertions.assertThat(result.status())
                .isEqualTo(roomescape.payment.service.dto.PaymentStatus.DONE);

        Payment updated = paymentRepository.findByOrderId(savedPayment.getOrderId()).get();
        Assertions.assertThat(updated.getStatus())
                .isEqualTo(PaymentStatus.CONFIRMED);

        Reservation confirmedReservation = reservationRepository.findById(savedReservation.getId()).get();
        Assertions.assertThat(confirmedReservation.getStatus())
                .isEqualTo(ReservationStatus.RESERVED);
    }
}
