package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidStateException;

class ReservationTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(1L, "우테코 공포물", "레벨2 미션의 공포", "/horror");


    @Test
    void 유효하지_않은_이름으로_생성_시_예외() {
        assertThatThrownBy(() -> new Reservation("", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Reservation(null, LocalDate.now(), time, theme, ReservationStatus.CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 날짜가_없으면_생성_시_예외() {
        assertThatThrownBy(() -> new Reservation("브라운", null, time, theme, ReservationStatus.CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 시간이_없으면_생성_시_예외() {
        assertThatThrownBy(() -> new Reservation("브라운", LocalDate.now(), null, theme, ReservationStatus.CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 같은_날짜와_시간인지_확인() {
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 1, 1), time, theme,
                ReservationStatus.WAITING);

        assertThat(reservation.isSameDateTime(LocalDate.of(2026, 1, 1), 1L)).isTrue();
        assertThat(reservation.isSameDateTime(LocalDate.of(2026, 1, 1), 2L)).isFalse();
        assertThat(reservation.isSameDateTime(LocalDate.of(2026, 1, 2), 1L)).isFalse();
    }

    @Test
    void 과거_날짜와_시간이면_예외() {
        Reservation pastReservation = new Reservation("브라운", LocalDate.now().minusDays(1), time, theme,
                ReservationStatus.WAITING);

        assertThatThrownBy(pastReservation::validateNotPast)
                .isInstanceOf(InvalidStateException.class)
                .hasMessage("이미 지난 시간/날짜는 예약할 수 없습니다.");

        Reservation futureReservation = new Reservation("브라운", LocalDate.now().plusDays(1), time, theme,
                ReservationStatus.WAITING);

        assertThatCode(futureReservation::validateNotPast)
                .doesNotThrowAnyException();
    }

    @Test
    void 결제_완료_시_상태_변경() {
        Reservation pending = new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.PENDING_PAYMENT);

        pending.completePayment("payment-key");

        assertThat(pending.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(pending.getPaymentKey()).isEqualTo("payment-key");
    }
}
