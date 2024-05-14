package roomescape.domain;

import static roomescape.TestFixture.DATE_AFTER_1DAY;
import static roomescape.TestFixture.MEMBER_BROWN;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.ROOM_THEME1;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import roomescape.exception.BadRequestException;

class ReservationTest {

    @DisplayName("사용자에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmptyName(Member value) {
        Assertions.assertThatThrownBy(() ->new Reservation(value, DATE_AFTER_1DAY, RESERVATION_TIME_10AM, ROOM_THEME1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("사용자에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("날짜에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmptyDate(LocalDate value) {
        Assertions.assertThatThrownBy(() ->new Reservation(MEMBER_BROWN, value, RESERVATION_TIME_10AM, ROOM_THEME1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("날짜에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("시간에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmptyTime(ReservationTime value) {
        Assertions.assertThatThrownBy(() ->new Reservation(MEMBER_BROWN, DATE_AFTER_1DAY, value, ROOM_THEME1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("시간에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("테마에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmptyTheme(RoomTheme value) {
        Assertions.assertThatThrownBy(() ->new Reservation(MEMBER_BROWN, DATE_AFTER_1DAY, RESERVATION_TIME_10AM, value))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("테마에 빈값을 입력할 수 없습니다.");
    }
}
