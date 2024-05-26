package roomescape.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.TestFixture;
import roomescape.exception.BadRequestException;

class ReservationTest {

    @DisplayName("사용자에 null이 들어가면 예외를 발생시킨다.")
    @Test
    void nullEmptyName() {
        Assertions.assertThatThrownBy(
                        () -> new Reservation(null, TestFixture.TOMORROW, TestFixture.getReservationTime10AM(),
                                TestFixture.getTheme1(),
                                Status.CONFIRMED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("사용자에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("날짜에 null이 들어가면 예외를 발생시킨다.")
    @Test
    void nullEmptyDate() {
        Assertions.assertThatThrownBy(
                        () -> new Reservation(TestFixture.getMember1(),
                                null, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(), Status.CONFIRMED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("날짜에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("시간에 null이 들어가면 예외를 발생시킨다.")
    @Test
    void nullEmptyTime() {
        Assertions.assertThatThrownBy(() -> new Reservation(TestFixture.getMember1(),
                        TestFixture.TOMORROW, null, TestFixture.getTheme1(), Status.CONFIRMED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("시간에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("테마에 null이 들어가면 예외를 발생시킨다.")
    @Test
    void nullEmptyTheme() {
        Assertions.assertThatThrownBy(
                        () -> new Reservation(TestFixture.getMember1(),
                                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), null, Status.CONFIRMED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("테마에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("상태에 null이 들어가면 예외를 발생시킨다.")
    @Test
    void nullEmptyStatus() {
        Assertions.assertThatThrownBy(
                        () -> new Reservation(TestFixture.getMember1(),
                                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("상태에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("예약 상태를 변경할때 null이 들어가면 예외를 발생시킨다.")
    @Test
    void updateStatusWithNull() {
        Assertions.assertThatThrownBy(() -> new Reservation().updateStatus(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("예약 상태가 입력되지 않았습니다.");
    }
}
