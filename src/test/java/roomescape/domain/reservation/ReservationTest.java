package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;

class ReservationTest {
    private final UserName userName = UserName.parse("아나키");
    private final LocalDate date = LocalDate.parse(TODAY);

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

    private final ThemeName themeName = ThemeName.parse("공포");
    private final Description description = Description.parse("너무무서워");
    private final ThumbnailUrl url = ThumbnailUrl.parse("/images/horror");
    private final Theme theme = new Theme(1L, themeName, description, url);

    @Test
    @DisplayName("올바른 정보로 예약을 생성하면 성공한다.")
    void createReservation_Success() {
        assertDoesNotThrow(() -> new Reservation(userName, date, time, theme));
    }

    @Test
    @DisplayName("예약자 이름이 null 이면 예외가 발생한다.")
    void createReservation_WhenUserNameIsNull_ThrowException() {
        UserName userName = null;

        assertThatThrownBy(() -> new Reservation(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약자 이름이 비어 있습니다.");
    }

    @Test
    @DisplayName("예약 날짜가 null 이면 예외가 발생한다.")
    void createReservation_WhenDateIsNull_ThrowException() {
        LocalDate date = null;

        assertThatThrownBy(() -> new Reservation(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약 날짜가 비어 있습니다.");
    }

    @Test
    @DisplayName("예약 시간이 null 이면 예외가 발생한다.")
    void createReservation_WhenTimeIsNull_ThrowException() {
        ReservationTime time = null;

        assertThatThrownBy(() -> new Reservation(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("시간이 비어 있습니다.");
    }

    @Test
    @DisplayName("테마가 null 이면 예외가 발생한다.")
    void createReservation_WhenThemeIsNull_ThrowException() {
        Theme theme = null;

        assertThatThrownBy(() -> new Reservation(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("테마가 비어 있습니다.");
    }
}
