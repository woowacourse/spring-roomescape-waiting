package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingTest {
    @Test
    @DisplayName("예약 대기 날짜는 예약 가능한 기간(오늘)보다 이전일 수 없다.")
    void given_earlierThenToday_when_create_then_throwException() {
        //given
        Member member = new Member(1L, "user@email.com", new Password("password", "salt"), "name", Role.USER);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        ReservationTime time = new ReservationTime(LocalTime.now());
        Theme theme = new Theme("테마1", "description", "thumbnail");

        //when, then
        assertThatThrownBy(() -> new Waiting(member, yesterday, time, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약 대기 시간은 예약 가능한 시간(오늘, 현재시간)보다 이전일 수 없다.")
    void given_earlierThenCurrentTime_when_create_then_throwException() {
        //given
        Member member = new Member(1L, "user@email.com", new Password("password", "salt"), "name", Role.USER);
        LocalDate date = LocalDate.now();
        ReservationTime earlierTime = new ReservationTime(LocalTime.now().minusHours(1));
        Theme theme = new Theme("테마1", "description", "thumbnail");

        //when, then
        assertThatThrownBy(() -> new Waiting(member, date, earlierTime, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
