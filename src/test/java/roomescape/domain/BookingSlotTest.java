package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createTimeFrom;
import static roomescape.TestFixture.fixedClockAt;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BookingSlotTest {

    @DisplayName("예약 슬롯이 과거인지 확인한다")
    @ParameterizedTest
    @CsvSource({
            "2025-01-01T23:59, 2025-01-02T00:00",
            "2025-01-01T12:00, 2025-01-02T12:00"
    })
    void isPast_true(LocalDateTime bookingDateTime, LocalDateTime currentDateTime) {
        // given
        LocalDate bookingDate = bookingDateTime.toLocalDate();
        ReservationTime bookingTime = createTimeFrom(bookingDateTime.toLocalTime());

        BookingSlot bookingSlot = new BookingSlot(
                bookingDate,
                bookingTime,
                createDefaultTheme()
        );

        // when
        Clock clock = fixedClockAt(currentDateTime);
        boolean past = bookingSlot.isPast(clock);

        // then
        assertThat(past).isTrue();
    }

    @DisplayName("예약 슬롯이 과거가 아닌지를 확인한다")
    @ParameterizedTest
    @CsvSource({
            "2025-01-02T00:00, 2025-01-01T00:00",
            "2025-01-01T00:01, 2025-01-01T00:00"
    })
    void isPast_false(LocalDateTime bookingDateTime, LocalDateTime currentDateTime) {
        // given
        LocalDate bookingDate = bookingDateTime.toLocalDate();
        ReservationTime bookingTime = createTimeFrom(bookingDateTime.toLocalTime());

        BookingSlot bookingSlot = new BookingSlot(
                bookingDate,
                bookingTime,
                createDefaultTheme()
        );

        // when
        Clock clock = fixedClockAt(currentDateTime);
        boolean past = bookingSlot.isPast(clock);

        // then
        assertThat(past).isFalse();
    }
}
