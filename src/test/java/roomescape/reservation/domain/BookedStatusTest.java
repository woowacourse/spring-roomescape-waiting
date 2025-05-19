package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookedStatusTest {

    @Test
    @DisplayName("BookedStatus를 true 값으로 생성 시 , sequence 값이 0이 된다.")
    void bookedStatusFromTrue() {
        // when
        // then
        BookedStatus actual = BookedStatus.from(true);
        int expected = 0;
        assertThat(actual.getSequence()).isEqualTo(expected);
    }

    @Test
    @DisplayName("BookedStatus를 true 값으로 생성 시 , sequence 값이 0이 된다.")
    void bookedStatusFromFalse() {
        // when
        // then
        BookedStatus actual = BookedStatus.from(false);
        int expected = 0;
        assertThat(actual.getSequence()).isNotEqualTo(expected);
    }
}
