package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorPolicy;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.exception.ReservationErrorCode.INVALID_RESERVATION_DATE;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.INVALID_RESERVATION_TIME;
import static roomescape.theme.exception.ThemeErrorCode.INVALID_THEME;

class ReservationSlotTest {

    private final ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
    private final Theme theme = Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

    @Test
    @DisplayName("예약 날짜가 null이면 도메인 예외가 발생한다.")
    void create_fail_when_date_is_null() {
        assertDomainException(
                () -> ReservationSlot.create(null, time, theme),
                INVALID_RESERVATION_DATE
        );
    }

    @Test
    @DisplayName("예약 시간이 null이면 도메인 예외가 발생한다.")
    void create_fail_when_time_is_null() {
        assertDomainException(
                () -> ReservationSlot.create(LocalDate.of(2023, 8, 5), null, theme),
                INVALID_RESERVATION_TIME
        );
    }

    @Test
    @DisplayName("예약 테마가 null이면 도메인 예외가 발생한다.")
    void create_fail_when_theme_is_null() {
        assertDomainException(
                () -> ReservationSlot.create(LocalDate.of(2023, 8, 5), time, null),
                INVALID_THEME
        );
    }

    private void assertDomainException(Runnable runnable, ErrorPolicy errorCode) {
        assertThatThrownBy(runnable::run)
                .isInstanceOfSatisfying(DomainException.class, exception ->
                        assertThat(exception.getErrorPolicy()).isEqualTo(errorCode)
                )
                .hasMessage(errorCode.message());
    }
}
