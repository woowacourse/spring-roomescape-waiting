package roomescape.domain.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.RoomEscapeFixture;

class ReservationTest {
    @ParameterizedTest
    @MethodSource("nullCases")
    void 매개변수에_NULL이_포함되면_예외가_발생한다(ReservationName reservationName, Slot slot, Status status) {
        assertThatThrownBy(() -> Reservation.create(reservationName, slot, status, LocalDateTime.MIN))
                .isInstanceOf(NullPointerException.class);
    }

    static Stream<Arguments> nullCases() {
        ReservationName name = new ReservationName("zeze");
        Slot slot = RoomEscapeFixture.slot().build();

        return Stream.of(
                Arguments.of(null, slot, Status.APPROVED),
                Arguments.of(name, null, Status.APPROVED),
                Arguments.of(name, slot, null)
        );
    }

    @Test
    void 과거_예약인지_비교할_수_있다() {
        Reservation past = RoomEscapeFixture.reservation().createdAt(RoomEscapeFixture.PAST_DATE_TIME).build();
        Reservation future = RoomEscapeFixture.reservation().id(2L).createdAt(RoomEscapeFixture.FUTURE_DATE_TIME)
                .build();

        Assertions.assertThat(past.isEarlierThan(future)).isTrue();
    }

    @Test
    void 시점이_같을때_id가_더_작으면_false를_반환한다() {
        Reservation id1WithSameDate = RoomEscapeFixture.reservation().build();
        Reservation id2WithSameDate = RoomEscapeFixture.reservation().createdAt(RoomEscapeFixture.PAST_DATE_TIME)
                .build();

        Assertions.assertThat(id1WithSameDate.isEarlierThan(id2WithSameDate)).isFalse();
    }

    @Test
    void 예약_일정이_제공된_시점보다_과거라면_예외가_발생한다() {
        Slot slot = RoomEscapeFixture.slot().date(RoomEscapeFixture.PAST_DATE).build();
        Reservation past = RoomEscapeFixture.reservation().slot(slot).build();

        Assertions.assertThatThrownBy(() -> past.isPastFrom(LocalDateTime.now()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_일정이_제공된_시점보다_미래라면_예외가_발생하지_않는다() {
        Slot slot = RoomEscapeFixture.slot().date(RoomEscapeFixture.FUTURE_DATE).build();
        Reservation future = RoomEscapeFixture.reservation().slot(slot).build();

        Assertions.assertThatCode(() -> future.isPastFrom(LocalDateTime.now()));
    }
}
