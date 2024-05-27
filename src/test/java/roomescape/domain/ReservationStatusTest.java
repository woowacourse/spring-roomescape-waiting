package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReservationStatusTest {

    @DisplayName("예약대기인지 판단한다.")
    @CsvSource(value = {"PENDING,true", "BOOKING,false"})
    @ParameterizedTest
    void isPending(ReservationStatus status, boolean expected) {
        boolean result = status.isPending();

        assertThat(result).isEqualTo(expected);
    }
}
