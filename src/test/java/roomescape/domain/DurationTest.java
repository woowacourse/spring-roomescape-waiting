package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DurationTest {

    @DisplayName("기간이 포함하는 날짜를 확인할 수 있다.")
    @Test
    void containsTest() {
        Duration duration = new Duration(
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 5, 31));

        assertAll(
                () -> assertThat(duration.contains(LocalDate.of(2024, 4, 30)))
                        .isFalse(),
                () -> assertThat(duration.contains(LocalDate.of(2024, 5, 1)))
                        .isTrue(),
                () -> assertThat(duration.contains(LocalDate.of(2024, 5, 30)))
                        .isTrue(),
                () -> assertThat(duration.contains(LocalDate.of(2024, 6, 1)))
                        .isFalse()
        );
    }
}
