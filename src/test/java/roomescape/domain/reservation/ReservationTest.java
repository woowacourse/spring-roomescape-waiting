package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.exception.InvalidRequestException;

class ReservationTest {

    static LocalDate date = LocalDate.of(2025, 5, 1);
    static Member member = new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password");
    static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
    static Theme theme = new Theme(1L, "우테코", "방탈출", ".png");
    static ReservationStatus status = ReservationStatus.RESERVED;

    @DisplayName("Date가 현재 시간 기준 미래인지 검증한다")
    @Test
    void validateFutureDateTest() {
        Reservation pastReservation = new Reservation(member, date, reservationTime, theme, status);
        assertThatThrownBy(() -> pastReservation.validateReservableTime(
                LocalDateTime.of(2025, 5, 1, 10, 1)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @DisplayName("Member가 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationMemberTest() {
        assertThatThrownBy(() -> new Reservation(null, date, reservationTime,  theme, status))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Date 이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationDateTimeTest() {
        assertThatThrownBy(() -> new Reservation(member, null, reservationTime,  theme, status))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("ReservationTime이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationTimeTest() {
        assertThatThrownBy(() -> new Reservation(member, date, null,  theme, status))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Theme이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationThemeTest() {
        assertThatThrownBy(() -> new Reservation(member, date, reservationTime,  null, status))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("status가 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationStatusTest() {
        assertThatThrownBy(() -> new Reservation(member, date, reservationTime,  theme, null))
                .isInstanceOf(NullPointerException.class);
    }
}
