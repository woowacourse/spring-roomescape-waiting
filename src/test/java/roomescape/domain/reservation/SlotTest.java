package roomescape.domain.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.RoomEscapeFixture;
import roomescape.domain.theme.Theme;

class SlotTest {
    @ParameterizedTest
    @MethodSource("nullCases")
    void 매개변수에_NULL이_포함되면_예외가_발생한다(ReservationDate date, ReservationTime time, Theme theme) {
        assertThatThrownBy(() -> Slot.create(date, time, theme))
                .isInstanceOf(NullPointerException.class);
    }

    static Stream<Arguments> nullCases() {
        ReservationDate date = new ReservationDate(LocalDate.now());
        ReservationTime time = ReservationTime.of(1L, LocalTime.now());
        Theme theme = RoomEscapeFixture.theme();

        return Stream.of(
                Arguments.of(null, time, theme),
                Arguments.of(date, null, theme),
                Arguments.of(date, time, null)
        );
    }

    @Test
    void 제공된_시점보다_과거의_슬롯이면_true를_반환한다() {
        Slot past = RoomEscapeFixture.slot().date(RoomEscapeFixture.PAST_DATE).build();

        Assertions.assertThat(past.isBefore(LocalDateTime.now())).isTrue();
    }

    @Test
    void 제공된_시점보다_미래의_슬롯이면_true를_반환한다() {
        Slot future = RoomEscapeFixture.slot().date(RoomEscapeFixture.FUTURE_DATE).build();

        Assertions.assertThat(future.isBefore(LocalDateTime.now())).isFalse();
    }
}
