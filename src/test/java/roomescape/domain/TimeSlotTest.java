package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TimeSlotTest {

    @DisplayName("이전 시간인지 판단한다.")
    @CsvSource(value = {"11:00,false", "12:00,false", "12:12,false", "12:13,true", "13:49,true"})
    @ParameterizedTest
    void isBefore(LocalTime given, boolean expected) {
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), given);
        TimeSlot timeSlot = new TimeSlot(LocalTime.parse("12:12"));

        boolean result = timeSlot.isBefore(dateTime);

        assertThat(result).isEqualTo(expected);
    }
}
