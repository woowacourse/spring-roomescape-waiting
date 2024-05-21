package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

class ReservationTest {

    @DisplayName("예약자가 null인 경우 예외가 발생한다.")
    @Test
    void createReservationExceptionTest() {
        assertThatCode(() -> new Reservation(null, LocalDate.now(), new Time(LocalTime.now()),
                new Theme(new ThemeName("test"), "desc", "thumb")))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("예약 날짜가 null인 경우 예외가 발생한다.")
    @Test
    void createReservationExceptionTest2() {
        assertThatCode(() -> new Reservation(
                new Member(new PlayerName("testName"),
                        new Email("test@test.com"),
                        new Password("testTest123!"),
                        Role.BASIC),
                null,
                new Time(LocalTime.now()),
                new Theme(new ThemeName("test"), "desc", "thumb")))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("예약 시간이 null인 경우 예외가 발생한다.")
    @Test
    void createReservationExceptionTest3() {
        assertThatCode(() -> new Reservation(
                new Member(new PlayerName("testName"),
                        new Email("test@test.com"),
                        new Password("testTest123!"),
                        Role.BASIC),
                LocalDate.now(),
                null,
                new Theme(new ThemeName("test"), "desc", "thumb")))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("테마가 null인 경우 예외가 발생한다.")
    @Test
    void createReservationExceptionTest4() {
        assertThatCode(() -> new Reservation(
                new Member(new PlayerName("testName"),
                        new Email("test@test.com"),
                        new Password("testTest123!"),
                        Role.BASIC),
                LocalDate.now(),
                new Time(LocalTime.now()),
                null))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("같은 회원이면 true를 반환한다.")
    @Test
    void isSameMemberTrueTest() {
        Reservation reservation = new Reservation(
                new Member(new PlayerName("testName"),
                        new Email("test@test.com"),
                        new Password("testTest123!"),
                        Role.BASIC),
                LocalDate.now(),
                new Time(LocalTime.now()),
                new Theme(new ThemeName("test"), "desc", "thumb"));
        Member member = new Member(new PlayerName("testName"), new Email("test@test.com"), new Password("testTest123!"),
                Role.BASIC);

        assertThat(reservation.isSameMember(member)).isTrue();
    }

    @DisplayName("예약이 현재보다 과거면 true를 반환한다.")
    @Test
    void isPastTrueTest() {
        Reservation reservation = new Reservation(
                new Member(new PlayerName("testName"),
                        new Email("test@test.com"),
                        new Password("testTest123!"),
                        Role.BASIC),
                LocalDate.now().minusDays(1),
                new Time(LocalTime.now()),
                new Theme(new ThemeName("test"), "desc", "thumb"));

        assertThat(reservation.isPast(Clock.fixed(Instant.now(), ZoneId.systemDefault()))).isTrue();
    }

    @DisplayName("예약이 현재보다 미래면 false를 반환한다.")
    @Test
    void isPastFalseTest() {
        Reservation reservation = new Reservation(
                new Member(new PlayerName("testName"),
                        new Email("test@test.com"),
                        new Password("testTest123!"),
                        Role.BASIC),
                LocalDate.now().plusDays(1),
                new Time(LocalTime.now()),
                new Theme(new ThemeName("test"), "desc", "thumb"));

        assertThat(reservation.isPast(Clock.fixed(Instant.now(), ZoneId.systemDefault()))).isFalse();
    }
}
