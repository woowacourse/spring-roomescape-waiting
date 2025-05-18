package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ReservationTimeTest {


    @DisplayName("isBefore 테스트")
    @ParameterizedTest
    @MethodSource
    void isBefore(final LocalTime time, final boolean expected) {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));

        // when
        final boolean actual = reservationTime.isBefore(time);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> isBefore() {
        return Stream.of(

                Arguments.of(LocalTime.of(11, 0), false),
                Arguments.of(LocalTime.of(12, 39), false),
                Arguments.of(LocalTime.of(12, 40), false),
                Arguments.of(LocalTime.of(12, 41), true),
                Arguments.of(LocalTime.of(15, 0), true)
        );
    }
}
