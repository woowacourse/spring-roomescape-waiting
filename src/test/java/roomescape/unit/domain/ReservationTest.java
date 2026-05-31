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
import roomescape.domain.exception.UnauthorizedException;

class ReservationTest {

    private static final Theme ANY_THEME = new Theme(1L, "공포", "설명", "https://example.com/horror.jpg");

    @Test
    void 지난_시각으로는_예약을_생성할_수_없다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThatThrownBy(() -> Reservation.createWith("브라운", LocalDate.of(2026, 5, 14), time, ANY_THEME, now))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("지난 시각에는 예약할 수 없습니다");
    }

    @Test
    void 미래_시각으로는_예약을_생성할_수_있다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(15, 0));
        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThatCode(() -> Reservation.createWith("브라운", LocalDate.of(2026, 5, 14), time, ANY_THEME, now))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약_시점이_현재보다_과거면_isPast가_true를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 5, 14), time, ANY_THEME);

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isTrue();
    }

    @Test
    void 예약_시점이_현재보다_미래면_isPast가_false를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(15, 0));
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 5, 14), time, ANY_THEME);

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isFalse();
    }

    @Test
    void 예약_시점과_현재가_정확히_같으면_isPast가_false를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 30));
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 5, 14), time, ANY_THEME);

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isFalse();
    }

    @Test
    void 예약자_이름과_같으면_isOwnedBy가_true를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 5, 14), time, ANY_THEME);

        assertThat(reservation.isOwnedBy("브라운")).isTrue();
    }

    @Test
    void 예약자_이름과_다르면_isOwnedBy가_false를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 5, 14), time, ANY_THEME);

        assertThat(reservation.isOwnedBy("티뉴")).isFalse();
    }

    @Test
    void 본인_예약이_아니면_취소할_수_없다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 5, 14), time, ANY_THEME);
        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 13, 0);

        assertThatThrownBy(() -> reservation.cancelBy("티뉴", now))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 지난_예약은_취소할_수_없다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 5, 14), time, ANY_THEME);
        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThatThrownBy(() -> reservation.cancelBy("브라운", now))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("취소할 수 없습니다");
    }

    @Test
    void 본인의_미래_예약은_취소할_수_있다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        Reservation reservation = new Reservation("브라운", LocalDate.of(2026, 5, 14), time, ANY_THEME);
        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 13, 0);

        assertThatCode(() -> reservation.cancelBy("브라운", now))
                .doesNotThrowAnyException();
    }
}
