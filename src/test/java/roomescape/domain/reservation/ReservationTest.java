package roomescape.domain.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.RoomEscapeFixture;

public class ReservationTest {
    @ParameterizedTest
    @MethodSource("nullCases")
    void 매개변수에_NULL이_포함되면_예외가_발생한다(ReservationName reservationName, Slot slot, Status status) {
        assertThatThrownBy(() -> Reservation.reserve(reservationName, slot, status, LocalDateTime.MIN))
                .isInstanceOf(NullPointerException.class);
    }

    static Stream<Arguments> nullCases() {
        ReservationName name = RoomEscapeFixture.reservationName();
        Slot slot = RoomEscapeFixture.slot();

        return Stream.of(
                Arguments.of(null, slot, Status.APPROVED),
                Arguments.of(name, null, Status.APPROVED),
                Arguments.of(name, slot, null)
        );
    }
}
