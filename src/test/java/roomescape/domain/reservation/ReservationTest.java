package roomescape.domain.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.RoomEscapeFixture;
import roomescape.domain.theme.Theme;

public class ReservationTest {
    @ParameterizedTest
    @MethodSource("nullCases")
    void 매개변수에_NULL이_포함되면_예외가_발생한다(Long id, ReservationName reservationName, ReservationDate date, ReservationTime time,
                                   Theme theme, Status status) {
        assertThatThrownBy(() -> Reservation.load(id, reservationName, date, time, theme, status, LocalDateTime.MIN))
                .isInstanceOf(NullPointerException.class);
    }

    static Stream<Arguments> nullCases() {
        Long id = 1L;
        ReservationName name = RoomEscapeFixture.reservationName();
        ReservationDate date = RoomEscapeFixture.reservationDate();
        ReservationTime time = RoomEscapeFixture.reservationTime();
        Theme theme = RoomEscapeFixture.theme();

        return Stream.of(
                Arguments.of(null, name, date, time, theme, Status.APPROVED),
                Arguments.of(id, null, date, time, theme, Status.APPROVED),
                Arguments.of(id, name, null, time, theme, Status.APPROVED),
                Arguments.of(id, name, date, null, theme, Status.APPROVED),
                Arguments.of(id, name, date, time, null, Status.APPROVED),
                Arguments.of(id, name, date, time, theme, null)
        );
    }
}
