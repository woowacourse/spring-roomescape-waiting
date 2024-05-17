package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION_DATE;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @DisplayName("생성 테스트")
    @Test
    void create() {
        assertThatCode(
            () -> new Reservation(1L, VALID_MEMBER, VALID_RESERVATION_DATE, VALID_RESERVATION_TIME, VALID_THEME))
            .doesNotThrowAnyException();

        assertThatCode(
            () -> new Reservation(VALID_MEMBER, VALID_RESERVATION_DATE, VALID_RESERVATION_TIME, VALID_THEME))
            .doesNotThrowAnyException();

    }

    @DisplayName("날짜를 null로 생성하면 예외가 발생한다.")
    @Test
    void create_WithNullDate() {
        assertThatThrownBy(
            () -> new Reservation(VALID_MEMBER, null, VALID_RESERVATION_TIME, VALID_THEME))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("시간을 null로 생성하면 예외가 발생한다.")
    @Test
    void create_WithNullTime() {
        assertThatThrownBy(
            () -> new Reservation(VALID_MEMBER, VALID_RESERVATION_DATE, null, VALID_THEME))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테마를 null로 생성하면 예외가 발생한다.")
    @Test
    void create_WithNullTheme() {
        assertThatThrownBy(
            () -> new Reservation(VALID_MEMBER, VALID_RESERVATION_DATE, VALID_RESERVATION_TIME, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("과거의 예약인지를 판단한다.")
    @Test
    void isPast() {
        Reservation reservation = new Reservation(
            VALID_MEMBER,
            new ReservationDate("1900-01-01"),
            VALID_RESERVATION_TIME,
            VALID_THEME);

        assertThat(reservation.isPast()).isTrue();
    }
}
