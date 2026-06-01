package roomescape.slot.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.date.domain.ReservationDate;
import roomescape.reservation.exception.ReservationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.exception.ReservationErrorInformation.*;

class ReservationSlotTest {

    private final LocalDate date = LocalDate.now().plusMonths(1);
    private final LocalTime startAt = LocalTime.of(15, 40);

    private final ReservationDate reservationDate = ReservationDate.create(date);
    private final ReservationTime reservationTime = ReservationTime.create(startAt);
    private final Theme theme = ThemeFixture.activeTheme();

    @Test
    @DisplayName("정상적인 테마/날자/시간으로 슬롯을 생성할 수 있다.")
    void of() {
        Assertions.assertThatCode(() -> ReservationSlot.of(reservationDate, reservationTime, theme))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 시간이 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateTime() {
        // given
        ReservationTime nullTime = null;

        // when & then
        assertThatThrownBy(() -> ReservationSlot.of(reservationDate, nullTime, theme))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_TIME_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("예약 날짜가 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateDate() {
        // given
        ReservationDate nullDate = null;

        // when & then
        assertThatThrownBy(() -> ReservationSlot.of(nullDate, reservationTime, theme))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_DATE_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("테마가 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateTheme() {
        // given
        Theme nullTheme = null;

        // when & then
        assertThatThrownBy(() -> ReservationSlot.of(reservationDate, reservationTime, nullTheme))
                .isInstanceOf(ReservationException.class)
                .hasMessage(RESERVATION_THEME_IS_NULL.getMessage());
    }

}
