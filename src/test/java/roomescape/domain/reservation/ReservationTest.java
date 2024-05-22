package roomescape.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static roomescape.TestFixture.*;

class ReservationTest {

    @Test
    @DisplayName("예약이 생성된다.")
    void createReservation() {
        assertThatCode(() -> new Reservation(MEMBER_MIA(), LocalDate.parse(DATE_MAY_EIGHTH), RESERVATION_TIME_SIX(), THEME_HORROR(), ReservationStatus.RESERVED))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("reservationsAndExpectedResult")
    @DisplayName("예약이 동일한 예약 시간을 갖는지 확인한다.")
    void hasSameDateTime(final LocalDate date, final String time, final boolean expectedResult) {
        // given
        final Reservation reservation = new Reservation(MEMBER_MIA(), LocalDate.parse(DATE_MAY_EIGHTH), RESERVATION_TIME_SIX(), THEME_HORROR(), ReservationStatus.RESERVED);

        // when
        final boolean actual = reservation.hasSameDateTime(date, new ReservationTime(time));

        // then
        assertThat(actual).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> reservationsAndExpectedResult() {
        return Stream.of(
                Arguments.of(DATE_MAY_EIGHTH, START_AT_SIX, true),
                Arguments.of(DATE_MAY_NINTH, START_AT_SEVEN, false)
        );
    }
}
