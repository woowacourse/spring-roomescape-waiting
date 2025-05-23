package roomescape.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingTest {

    @DisplayName("현재보다 과거의 예약에 대한 대기인 경우 예외가 발생한다.")
    @Test
    void isAfterByNow() {
        //given
        Waiting waiting = Waiting.of(
                LocalDate.now().minusDays(1),
                new Theme("test", "test", "test"),
                new ReservationTime(LocalTime.of(20, 0)),
                new Member("test", "test", "test", Role.USER)
        );

        LocalDate now = LocalDate.now().plusDays(1);

        //when //then
        assertThatThrownBy(() -> waiting.isAfterBy(now))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("과거 및 당일 예약은 대기 신청이 불가능합니다.");
    }

    @DisplayName("현재보다 미래의 예약에 대한 대기인 경우 예외가 발생하지 않는다.")
    @Test
    void isBeforeByNow() {
        //given
        Waiting waiting = Waiting.of(
                LocalDate.now().plusDays(2),
                new Theme("test", "test", "test"),
                new ReservationTime(LocalTime.of(20, 0)),
                new Member("test", "test", "test", Role.USER)
        );

        LocalDate now = LocalDate.now().plusDays(1);

        //when //then
        assertThatCode(() -> waiting.isAfterBy(now))
                .doesNotThrowAnyException();
    }

}
