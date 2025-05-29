package roomescape.business.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingTest {

    @Test
    @DisplayName("date 필드에 null 들어오면 예외가 발생한다")
    void validateDate() {
        //given
        final LocalDate invalidDate = null;
        final Member member = new Member(1L, "name", "role", "email", "password");
        final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        final Theme theme = new Theme(1L, "name", "description", "thumbnail");

        //when & then
        Assertions.assertThatThrownBy(
                () -> new Waiting(member, theme, reservationTime, invalidDate, LocalDateTime.now()));
    }

    @Test
    @DisplayName("인자값보다 예약 대기 날짜가 과거이면 true를 반환한다")
    void isPastWhenDateIsBeforeToday() {
        // given
        LocalDate reservationDate = LocalDate.of(2024, 12, 31);
        LocalTime reservationTime = LocalTime.of(14, 0);

        ReservationTime time = new ReservationTime(1L, reservationTime);
        Member member = new Member(1L, "mimi", "USER", "email@test.com", "pass");
        Theme theme = new Theme(1L, "theme", "test", "thumbnail");
        Waiting waiting = new Waiting(member, theme, time, reservationDate, LocalDateTime.now());

        // when
        LocalDate futureDate = LocalDate.of(2025, 1, 1);
        boolean result = waiting.isPast(LocalDateTime.of(futureDate, reservationTime));

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("인자값과 예약 대기 날짜가 같고 인자값이 예약 대기 시간보다 과거이면 false를 반환한다")
    void isPastWhenDateIsTodayAndTimeIsPast() {
        // given
        LocalDate reservationDate = LocalDate.of(2024, 12, 31);
        LocalTime reservationTime = LocalTime.of(14, 0);

        ReservationTime time = new ReservationTime(1L, reservationTime);
        Member member = new Member(1L, "mimi", "USER", "email@test.com", "pass");
        Theme theme = new Theme(1L, "theme", "test", "thumbnail");
        Waiting waiting = new Waiting(member, theme, time, reservationDate, LocalDateTime.now());

        // when
        LocalTime pastTime = LocalTime.of(10, 0);
        boolean result = waiting.isPast(LocalDateTime.of(reservationDate, pastTime));

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("인자값과 예약 대기 날짜가 같고 예약 대기 시간이 인자값보다 과거이면 true를 반환한다")
    void isPastWhenDateIsTodayAndTimeIsFuture() {
        // given
        LocalDate reservationDate = LocalDate.of(2024, 12, 31);
        LocalTime reservationTime = LocalTime.of(14, 0);

        ReservationTime time = new ReservationTime(1L, reservationTime);
        Member member = new Member(1L, "mimi", "USER", "email@test.com", "pass");
        Theme theme = new Theme(1L, "theme", "test", "thumbnail");
        Waiting waiting = new Waiting(member, theme, time, reservationDate, LocalDateTime.now());

        // when
        LocalTime futureTime = LocalTime.of(16, 0);
        boolean result = waiting.isPast(LocalDateTime.of(reservationDate, futureTime));

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("예약 대기 날짜가 인자값보다 미래이면 false를 반환한다")
    void isPastWhenDateIsAfterToday() {
        // given
        LocalDate reservationDate = LocalDate.of(2024, 12, 31);
        LocalTime reservationTime = LocalTime.of(14, 0);

        ReservationTime time = new ReservationTime(1L, reservationTime);
        Member member = new Member(1L, "mimi", "USER", "email@test.com", "pass");
        Theme theme = new Theme(1L, "theme", "test", "thumbnail");
        Waiting waiting = new Waiting(member, theme, time, reservationDate, LocalDateTime.now());

        // when
        LocalDate pastDate = LocalDate.of(2024, 12, 30);
        boolean result = waiting.isPast(LocalDateTime.of(pastDate, reservationTime));

        // then
        assertThat(result).isFalse();
    }
}
