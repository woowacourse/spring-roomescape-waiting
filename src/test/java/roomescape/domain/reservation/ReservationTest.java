package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.theme.Description;
import roomescape.domain.reservation.theme.ThemeName;
import roomescape.domain.reservation.theme.ThumbnailUrl;
import roomescape.domain.reservation.time.ReservationTime;
import roomescape.domain.reservation.theme.Theme;

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
    void 정상_예약_테스트() {
        assertDoesNotThrow(() -> new Reservation(userName, date, time, theme));
    }

    @Test
    @DisplayName("예약자 이름이 null 이면 예외가 발생한다.")
    void 이름이_null_예외_테스트() {
        UserName userName = null;

        assertThatThrownBy(() -> new Reservation(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약자 이름이 비어 있습니다.");
    }

    @Test
    @DisplayName("예약 날짜가 null 이면 예외가 발생한다.")
    void 날짜가_null_예외_테스트() {
        LocalDate date = null;

        assertThatThrownBy(() -> new Reservation(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약 날짜가 비어 있습니다.");
    }

    @Test
    @DisplayName("예약 시간이 null 이면 예외가 발생한다.")
    void 시간이_null_예외_테스트() {
        ReservationTime time = null;

        assertThatThrownBy(() -> new Reservation(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("시간이 비어 있습니다.");
    }

    @Test
    @DisplayName("테마가 null 이면 예외가 발생한다.")
    void 테마가_null_예외_테스트() {
        Theme theme = null;

        assertThatThrownBy(() -> new Reservation(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("테마가 비어 있습니다.");
    }
}
