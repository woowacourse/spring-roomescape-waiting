package roomescape.domain.time.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTimeTest {

    static Stream<Arguments> timeProvider() {
        return Stream.of(
                Arguments.of(new ReservationTime(1L, LocalTime.of(9, 0)), true),
                Arguments.of(new ReservationTime(2L, LocalTime.of(10, 0)), true),
                Arguments.of(new ReservationTime(3L, LocalTime.of(11, 0)), true),
                Arguments.of(new ReservationTime(4L, LocalTime.of(12, 0)), true),
                Arguments.of(new ReservationTime(5L, LocalTime.of(13, 0)), false)
        );
    }

    @DisplayName("예약된 시간인지 확인할 수 있다.")
    @MethodSource("timeProvider")
    @ParameterizedTest
    void isBookedTest(ReservationTime reservationTime, boolean expected) {
        List<ReservationTime> bookedTimes = List.of(
                new ReservationTime(1L, LocalTime.of(9, 0)),
                new ReservationTime(2L, LocalTime.of(10, 0)),
                new ReservationTime(3L, LocalTime.of(11, 0)),
                new ReservationTime(4L, LocalTime.of(12, 0))
        );

        boolean actual = reservationTime.isBooked(bookedTimes);

        assertThat(actual).isEqualTo(expected);
    }
}
