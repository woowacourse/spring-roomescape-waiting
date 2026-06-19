package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.common.config.FixedClockConfig.NOW_TIME;
import static roomescape.common.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.ForbiddenException;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;

public class WaitingTest {
    private final UserName userName = UserName.parse("아나키");
    private final LocalDate date = LocalDate.parse(TODAY);

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

    private final ThemeName themeName = ThemeName.parse("공포");
    private final Description description = Description.parse("너무무서워");
    private final ThumbnailUrl url = ThumbnailUrl.parse("/images/horror");
    private final Theme theme = new Theme(1L, themeName, description, url);
    private final LocalDateTime createdAt = LocalDateTime.of(
            LocalDate.parse(TODAY),
            LocalTime.parse(NOW_TIME)
    );


    @Test
    @DisplayName("올바른 정보로 예약 대기를 생성하면 성공한다.")
    void createWaiting_Success() {
        assertDoesNotThrow(() -> new Waiting(userName, date, time, theme, createdAt));
    }

    @Test
    @DisplayName("예약자 이름이 null 이면 예외가 발생한다.")
    void createWaiting_WhenUserNameIsNull_ThrowException() {
        UserName userName = null;

        assertThatThrownBy(() -> new Waiting(userName, date, time, theme, createdAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약자 이름이 비어 있습니다.");
    }

    @Test
    @DisplayName("예약 날짜가 null 이면 예외가 발생한다.")
    void createWaiting_WhenDateIsNull_ThrowException() {
        LocalDate date = null;

        assertThatThrownBy(() -> new Waiting(userName, date, time, theme, createdAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약 날짜가 비어 있습니다.");
    }

    @Test
    @DisplayName("예약 시간이 null 이면 예외가 발생한다.")
    void createWaiting_WhenTimeIsNull_ThrowException() {
        ReservationTime time = null;

        assertThatThrownBy(() -> new Waiting(userName, date, time, theme, createdAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("시간이 비어 있습니다.");
    }

    @Test
    @DisplayName("테마가 null 이면 예외가 발생한다.")
    void createWaiting_WhenThemeIsNull_ThrowException() {
        Theme theme = null;

        assertThatThrownBy(() -> new Waiting(userName, date, time, theme, createdAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("테마가 비어 있습니다.");
    }

    @Test
    @DisplayName("대기 신청 시간이 null 이면 예외가 발생한다.")
    void createWaiting_WhenCreatedAtIsNull_ThrowException() {
        LocalDateTime createdAt = null;

        assertThatThrownBy(() -> new Waiting(userName, date, time, theme, createdAt))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("대기 신청 시간이 비어 있습니다.");
    }

    @Test
    @DisplayName("본인의 예약 대기를 취소하면 성공한다.")
    void cancel_WhenOwner_Success() {
        Waiting waiting = new Waiting(userName, date, time, theme, createdAt);

        assertDoesNotThrow(() -> waiting.cancel(userName));
    }

    @Test
    @DisplayName("다른 사람의 예약 대기를 취소하면 예외가 발생한다.")
    void cancel_WhenNotOwner_ThrowException() {
        Waiting waiting = new Waiting(userName, date, time, theme, createdAt);
        UserName otherUser = UserName.parse("다른사람");

        assertThatThrownBy(() -> waiting.cancel(otherUser))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("다른 사람의 예약 대기는 취소할 수 없습니다.");
    }
}
