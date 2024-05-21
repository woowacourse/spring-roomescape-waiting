package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

class ReservationTest {

    private static final LocalDate DATE = LocalDate.of(2024, 5, 5);
    private static final Member MEMBER = new Member("exmaple@gmail.com", "abc123", "구름", Role.USER);
    private static final ReservationTime RESERVATION_TIME = new ReservationTime(LocalTime.of(10, 0));
    private static final Theme THEME = new Theme("테마", "테마 설명", "https://example.com");

    @Test
    @DisplayName("예약을 생성한다.")
    void create() {
        assertThatCode(() -> new Reservation(DATE, MEMBER, RESERVATION_TIME, THEME))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("날짜가 없으면 예외가 발생한다.")
    void validateDate() {
        assertThatThrownBy(() -> new Reservation(null, MEMBER, RESERVATION_TIME, THEME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("날짜는 필수 값입니다.");
    }

    @Test
    @DisplayName("회원이 없으면 예외가 발생한다.")
    void validateMember() {
        assertThatThrownBy(() -> new Reservation(DATE, null, RESERVATION_TIME, THEME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원은 필수 값입니다.");
    }

    @Test
    @DisplayName("예약 시간이 없으면 예외가 발생한다.")
    void validateTime() {
        assertThatThrownBy(() -> new Reservation(DATE, MEMBER, null, THEME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시간은 필수 값입니다.");
    }

    @Test
    @DisplayName("테마가 없으면 예외가 발생한다.")
    void validateTheme() {
        assertThatThrownBy(() -> new Reservation(DATE, MEMBER, RESERVATION_TIME, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마는 필수 값입니다.");
    }
}
