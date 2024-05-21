package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

class WaitingTest {

    @DisplayName("예약자가 없으면 예외가 발생한다.")
    @Test
    void createExceptionTest() {
        Member member = null;
        LocalDate date = LocalDate.now();
        Time time = new Time(LocalTime.now());
        Theme theme = new Theme(new ThemeName("test"), "desc", "thumb");

        assertThatCode(() -> new Waiting(member, date, time, theme))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("날짜가 없으면 예외가 발생한다.")
    @Test
    void createExceptionTest2() {
        Member member = new Member(new PlayerName("test"), new Email("test@test.com"), new Password("testTest1!"), Role.BASIC);
        LocalDate date = null;
        Time time = new Time(LocalTime.now());
        Theme theme = new Theme(new ThemeName("test"), "desc", "thumb");

        assertThatCode(() -> new Waiting(member, date, time, theme))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("시간이 없으면 예외가 발생한다.")
    @Test
    void createExceptionTest3() {
        Member member = new Member(new PlayerName("test"), new Email("test@test.com"), new Password("testTest1!"), Role.BASIC);
        LocalDate date = LocalDate.now();
        Time time = null;
        Theme theme = new Theme(new ThemeName("test"), "desc", "thumb");
        assertThatCode(() -> new Waiting(member, date, time, theme))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("테마가 없으면 예외가 발생한다.")
    @Test
    void createExceptionTest4() {
        Member member = new Member(new PlayerName("test"), new Email("test@test.com"), new Password("testTest1!"), Role.BASIC);
        LocalDate date = LocalDate.now();
        Time time = new Time(LocalTime.now());
        Theme theme = null;
        assertThatCode(() -> new Waiting(member, date, time, theme))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }
}
