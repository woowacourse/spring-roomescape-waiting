package roomescape.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ForbiddenException;

class ReservationTest {

    private static final Theme ANY_THEME = new Theme(1L, "공포", "설명", "https://example.com/horror.jpg");
    private static final String RESERVATION_OWNER = "브라운";
    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 5, 14);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 14, 14, 30);
    private static final LocalDateTime BEFORE_RESERVATION = LocalDateTime.of(2026, 5, 14, 13, 0);
    private static final ReservationTime TWO_PM = new ReservationTime(1L, LocalTime.of(14, 0));
    private static final ReservationTime TWO_THIRTY_PM = new ReservationTime(1L, LocalTime.of(14, 30));
    private static final ReservationTime THREE_PM = new ReservationTime(1L, LocalTime.of(15, 0));

    @Test
    void 지난_시각으로는_예약을_생성할_수_없다() {
        assertThatThrownBy(() -> Reservation.createWith(RESERVATION_OWNER, RESERVATION_DATE, TWO_PM, ANY_THEME, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("지난 시각에는 예약할 수 없습니다");
    }

    @Test
    void 미래_시각으로는_예약을_생성할_수_있다() {
        assertThatCode(() -> Reservation.createWith(RESERVATION_OWNER, RESERVATION_DATE, THREE_PM, ANY_THEME, NOW))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약_시점이_현재보다_과거면_isPast가_true를_반환한다() {
        Reservation reservation = new Reservation(RESERVATION_OWNER, RESERVATION_DATE, TWO_PM, ANY_THEME);

        assertThat(reservation.isPast(NOW)).isTrue();
    }

    @Test
    void 예약_시점이_현재보다_미래면_isPast가_false를_반환한다() {
        Reservation reservation = new Reservation(RESERVATION_OWNER, RESERVATION_DATE, THREE_PM, ANY_THEME);

        assertThat(reservation.isPast(NOW)).isFalse();
    }

    @Test
    void 예약_시점과_현재가_정확히_같으면_isPast가_false를_반환한다() {
        Reservation reservation = new Reservation(RESERVATION_OWNER, RESERVATION_DATE, TWO_THIRTY_PM, ANY_THEME);

        assertThat(reservation.isPast(NOW)).isFalse();
    }

    @Test
    void 예약자_이름과_같으면_isOwnedBy가_true를_반환한다() {
        Reservation reservation = new Reservation(RESERVATION_OWNER, RESERVATION_DATE, TWO_PM, ANY_THEME);

        assertThat(reservation.isOwnedBy(RESERVATION_OWNER)).isTrue();
    }

    @Test
    void 예약자_이름과_다르면_isOwnedBy가_false를_반환한다() {
        Reservation reservation = new Reservation(RESERVATION_OWNER, RESERVATION_DATE, TWO_PM, ANY_THEME);

        assertThat(reservation.isOwnedBy("티뉴")).isFalse();
    }

    @Test
    void 본인_예약이_아니면_취소할_수_없다() {
        Reservation reservation = new Reservation(RESERVATION_OWNER, RESERVATION_DATE, TWO_PM, ANY_THEME);

        assertThatThrownBy(() -> reservation.cancelBy("티뉴", BEFORE_RESERVATION))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void 지난_예약은_취소할_수_없다() {
        Reservation reservation = new Reservation(RESERVATION_OWNER, RESERVATION_DATE, TWO_PM, ANY_THEME);

        assertThatThrownBy(() -> reservation.cancelBy(RESERVATION_OWNER, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("취소할 수 없습니다");
    }

    @Test
    void 본인의_미래_예약은_취소할_수_있다() {
        Reservation reservation = new Reservation(RESERVATION_OWNER, RESERVATION_DATE, TWO_PM, ANY_THEME);

        assertThatCode(() -> reservation.cancelBy(RESERVATION_OWNER, BEFORE_RESERVATION))
                .doesNotThrowAnyException();
    }
}
