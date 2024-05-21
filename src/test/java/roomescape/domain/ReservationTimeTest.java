package roomescape.domain;

import static roomescape.TestFixture.MEMBER_BROWN;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;

import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import roomescape.exception.BadRequestException;

class ReservationTimeTest {

    @DisplayName("시간에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmptyTime(LocalTime value) {
        Assertions.assertThatThrownBy(() -> new ReservationTime(value))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("시간에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("중복된 예약시간이 존재하면 예외를 발생시킨다.")
    @Test
    void duplicatedTime() {
        // given
        ReservationTime reservationTime = RESERVATION_TIME_10AM;
        ReservationTime comparedReservationTime = new ReservationTime(reservationTime.getStartAt());

        // when & then
        Assertions.assertThatThrownBy(() -> reservationTime.validateDuplicatedTime(comparedReservationTime))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("중복된 시간을 생성할 수 없습니다.");
    }
}
