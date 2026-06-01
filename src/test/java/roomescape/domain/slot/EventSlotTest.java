package roomescape.domain.slot;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;

class EventSlotTest {

    private final LocalDate date = LocalDate.now();
    private final ReservationTime time = new ReservationTime(1L, LocalTime.now());
    private final Theme theme = new Theme(1L, ThemeName.parse("테마"), Description.parse("설명"),
            ThumbnailUrl.parse("/images/url"));

    @Test
    @DisplayName("과거 날짜로 예약 대기하면 예외가 발생한다.")
    void verifyBookable_WhenDateIsPast_ThrowException() {
        EventSlot eventSlot = new EventSlot(date.minusDays(1), time, theme);
        LocalDateTime now = LocalDateTime.now();

        assertThatThrownBy(() -> eventSlot.verifyBookable(now))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("과거 날짜로는 예약 대기를 할 수 없습니다.");
    }

    @Test
    @DisplayName("과거 시간으로 예약 대기하면 예외가 발생한다.")
    void verifyBookable_WhenTimeIsPast_ThrowException() {
        EventSlot eventSlot = new EventSlot(date, new ReservationTime(1L, LocalTime.now().minusMinutes(1)), theme);
        LocalDateTime now = LocalDateTime.now();

        assertThatThrownBy(() -> eventSlot.verifyBookable(now))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("이미 지난 시간으로 예약 대기를 할 수 없습니다.");
    }

    @Test
    @DisplayName("미래 시간으로 예약 대기하면 성공한다.")
    void verifyBookable_WhenDateTimeIsFuture_Success() {
        EventSlot eventSlot = new EventSlot(date.plusDays(1), time, theme);
        LocalDateTime now = LocalDateTime.now();

        assertDoesNotThrow(() -> eventSlot.verifyBookable(now));
    }
}
