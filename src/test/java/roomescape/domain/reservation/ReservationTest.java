package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.common.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.domain.slot.EventSlot;
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

    private final EventSlot eventSlot = new EventSlot(date, time, theme);

    @Test
    @DisplayName("올바른 정보로 예약을 생성하면 성공한다.")
    void createReservation_Success() {
        assertDoesNotThrow(() -> Reservation.createPending(userName, date, time, theme));
    }

    @Test
    @DisplayName("예약자 이름이 null 이면 예외가 발생한다.")
    void createReservation_WhenUserNameIsNull_ThrowNullPointerException() {
        UserName userName = null;

        assertThatThrownBy(() -> Reservation.createPending(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약자 이름이 비어 있습니다.");
    }

    @Test
    @DisplayName("예약 날짜가 null 이면 예외가 발생한다.")
    void createReservation_WhenDateIsNull_ThrowNullPointerException() {
        LocalDate date = null;

        assertThatThrownBy(() -> Reservation.createPending(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("예약 날짜가 비어 있습니다.");
    }

    @Test
    @DisplayName("예약 시간이 null 이면 예외가 발생한다.")
    void createReservation_WhenTimeIsNull_ThrowNullPointerException() {
        ReservationTime time = null;

        assertThatThrownBy(() -> Reservation.createPending(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("시간이 비어 있습니다.");
    }

    @Test
    @DisplayName("테마가 null 이면 예외가 발생한다.")
    void createReservation_WhenThemeIsNull_ThrowNullPointerException() {
        Theme theme = null;

        assertThatThrownBy(() -> Reservation.createPending(userName, date, time, theme))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("테마가 비어 있습니다.");
    }

    @Test
    @DisplayName("과거 날짜로 예약하면 예외가 발생한다.")
    void verifyBookable_WhenDateIsPast_ThrowUnprocessableEntityException() {
        Reservation reservation = Reservation.createPending(userName, date, time, theme);
        LocalDateTime now = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> reservation.verifyBookable(now))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("과거 날짜로는 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("과거 시간으로 예약하면 예외가 발생한다.")
    void verifyBookable_WhenTimeIsPast_ThrowUnprocessableEntityException() {
        Reservation reservation = Reservation.createPending(userName, date, time, theme);
        LocalDateTime now = LocalDateTime.of(date, time.getStartAt().plusMinutes(1));

        assertThatThrownBy(() -> reservation.verifyBookable(now))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("이미 지난 시간으로 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("다른 사람의 예약을 변경하면 예외가 발생한다.")
    void change_WhenNotOwner_ThrowForbiddenException() {
        Reservation reservation = Reservation.createPending(userName, date, time, theme);
        UserName otherUser = UserName.parse("다른사람");
        LocalDateTime now = LocalDateTime.now();

        assertThatThrownBy(() -> reservation.change(otherUser, date.plusDays(1), time, now))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("다른 사람의 예약은 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("과거 시간으로 예약을 변경하면 예외가 발생한다.")
    void change_WhenTimeIsPastDateTime_ThrowUnprocessableEntityException() {
        Reservation reservation = Reservation.createPending(userName, date, time, theme);
        LocalDateTime now = LocalDateTime.now();

        assertThatThrownBy(() -> reservation.change(userName, date.minusDays(1), time, now))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("과거의 시간으로 예약을 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("다른 사람의 예약을 취소하면 예외가 발생한다.")
    void cancel_WhenNotOwner_ThrowForbiddenException() {
        Reservation reservation = Reservation.restoreConfirmed(null, userName, eventSlot);
        UserName otherUser = UserName.parse("다른사람");
        LocalDateTime now = LocalDateTime.now().minusDays(1);

        assertThatThrownBy(() -> reservation.cancel(otherUser, now))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("다른 사람의 예약은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 지난 예약을 취소하면 예외가 발생한다.")
    void cancel_WhenAlreadyPassed_ThrowUnprocessableEntityException() {
        Reservation reservation = Reservation.restoreConfirmed(1L, userName, eventSlot); //10:00
        LocalDateTime now = LocalDateTime.of(date, time.getStartAt()).plusMinutes(1); // 10:01

        assertThatThrownBy(() -> reservation.cancel(userName, now))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("이미 지난 예약은 취소할 수 없습니다.");
    }
}
