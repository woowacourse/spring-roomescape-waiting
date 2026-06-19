package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;

class ScheduleTest {

    private final Theme theme = new Theme(1L, "잠긴 방", "설명", "https://example.com/theme.jpg", 20000);
    private final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

    @DisplayName("스케줄은 테마, 날짜, 시간을 저장한다.")
    @Test
    void create() {
        Schedule schedule = new Schedule(1L, theme, LocalDate.of(2026, 7, 1), reservationTime);

        assertThat(schedule.getId()).isEqualTo(1L);
        assertThat(schedule.getTheme()).isEqualTo(theme);
        assertThat(schedule.getDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(schedule.getTime()).isEqualTo(reservationTime);
    }

    @DisplayName("ID가 null이어도 아직 저장 전 도메인으로 생성할 수 있다.")
    @Test
    void nullId() {
        Schedule schedule = new Schedule(null, theme, LocalDate.of(2026, 7, 1), reservationTime);

        assertThat(schedule.getId()).isNull();
    }

    @DisplayName("테마, 날짜, 시간은 null일 수 없다.")
    @Test
    void requiredFields() {
        assertInvalidInput(() -> new Schedule(1L, null, LocalDate.of(2026, 7, 1), reservationTime));
        assertInvalidInput(() -> new Schedule(1L, theme, null, reservationTime));
        assertInvalidInput(() -> new Schedule(1L, theme, LocalDate.of(2026, 7, 1), null));
    }

    private void assertInvalidInput(Runnable runnable) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.INVALID_INPUT);
    }
}
