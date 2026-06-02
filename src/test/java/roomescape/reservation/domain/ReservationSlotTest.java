package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationSlotTest {

    private final ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));
    private final Theme theme = Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

    @ParameterizedTest
    @CsvSource(value = {
            "2025-05-11T09:59:59,false",
            "2025-05-11T10:00:00,false",
            "2025-05-11T10:00:01,true",
    })
    void isPassed(LocalDateTime now, boolean expected) {
        ReservationSlot slot = ReservationSlot.of(LocalDate.of(2025, 5, 11), time, theme);

        boolean result = slot.isPassed(now);

        assertThat(result).isEqualTo(expected);
    }
}
