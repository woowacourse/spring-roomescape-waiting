package roomescape.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.config.TestTimeConfig;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.FakePaymentGateway;
import roomescape.payment.FakePaymentGatewayConfig;
import roomescape.payment.dto.PaymentConfirmRequest;
import roomescape.payment.dto.PaymentConfirmResponse;
import roomescape.payment.exception.PaymentErrorCode;
import roomescape.reservation.dto.ReservationCreateResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest
@Import({TestTimeConfig.class, FakePaymentGatewayConfig.class})
@Sql(scripts = "/empty.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PaymentServiceTest {

    private static final LocalDate FUTURE_DATE = LocalDate.parse("2026-08-05");

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private FakePaymentGateway fakePaymentGateway;

    private ReservationCreateResponse reservePending() {
        Long timeId = reservationTimeRepository.save(ReservationTime.create(LocalTime.parse("10:00"))).getId();
        Long themeId = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")).getId();
        return reservationService.reserve(new ReservationRequest("브라운", FUTURE_DATE, timeId, themeId));
    }

    @Test
    void 결제를_승인하면_주문이_완료되고_예약이_확정된다() {
        // given
        ReservationCreateResponse reservation = reservePending();
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                "test_payment_key", reservation.payment().orderId(), reservation.payment().amount());

        // when
        PaymentConfirmResponse response = paymentService.confirm(request);

        // then
        assertThat(response.status()).isEqualTo("DONE");
        assertThat(response.paymentKey()).isNotBlank();
        assertThat(reservationService.readById(reservation.id()).status()).isEqualTo("CONFIRMED");
    }

    @Test
    void 주문_금액과_다르면_승인_호출_전에_차단된다() {
        // given
        ReservationCreateResponse reservation = reservePending();
        PaymentConfirmRequest tampered = new PaymentConfirmRequest(
                "test_payment_key", reservation.payment().orderId(), reservation.payment().amount() + 1);

        // when & then
        assertThatThrownBy(() -> paymentService.confirm(tampered))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        assertThat(fakePaymentGateway.callCount()).isZero();
        assertThat(reservationService.readById(reservation.id()).status()).isEqualTo("PENDING");
    }

    @Test
    void 존재하지_않는_주문번호로_승인하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> paymentService.confirm(
                new PaymentConfirmRequest("test_payment_key", "ORDER-unknown", 1000L)))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.ORDER_NOT_FOUND);
    }
}
