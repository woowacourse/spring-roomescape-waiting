package roomescape.domain;

import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.THEME1;
import static roomescape.TestFixture.TOMORROW;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BadRequestException;

class ReservationTest {

    @DisplayName("사용자에 null이 들어가면 예외를 발생시킨다.")
    @Test
    void nullEmptyName() {
        Assertions.assertThatThrownBy(
                        () -> new Reservation(null, TOMORROW, RESERVATION_TIME_10AM, THEME1, Status.CONFIRMED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("사용자에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("날짜에 null이 들어가면 예외를 발생시킨다.")
    @Test
    void nullEmptyDate() {
        Assertions.assertThatThrownBy(
                        () -> new Reservation(MEMBER1, null, RESERVATION_TIME_10AM, THEME1, Status.CONFIRMED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("날짜에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("시간에 null이 들어가면 예외를 발생시킨다.")
    @Test
    void nullEmptyTime() {
        Assertions.assertThatThrownBy(() -> new Reservation(MEMBER1, TOMORROW, null, THEME1, Status.CONFIRMED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("시간에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("테마에 null이 들어가면 예외를 발생시킨다.")
    @Test
    void nullEmptyTheme() {
        Assertions.assertThatThrownBy(
                        () -> new Reservation(MEMBER1, TOMORROW, RESERVATION_TIME_10AM, null, Status.CONFIRMED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("테마에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("상태에 null이 들어가면 예외를 발생시킨다.")
    @Test
    void nullEmptyStatus() {
        Assertions.assertThatThrownBy(
                        () -> new Reservation(MEMBER1, TOMORROW, RESERVATION_TIME_10AM, THEME1, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("상태에 빈값을 입력할 수 없습니다.");
    }
}
