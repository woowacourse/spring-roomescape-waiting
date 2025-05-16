package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class BookedStatusTest {

    @Test
    @DisplayName("유효한 예약 상태로 BookedStatus 객체를 생성할 수 있다")
    void createValidBookedStatus() {
        // when
        final BookedStatus bookedStatus = BookedStatus.from(0);

        // then
        assertAll(() -> {
            assertThat(bookedStatus).isNotNull();
            assertThat(bookedStatus.getSequence()).isEqualTo(0);
            assertThat(bookedStatus.isBooked()).isTrue();
        });
    }

    @Test
    @DisplayName("sequence가 0이 아닌 경우 isBooked()는 false를 반환한다")
    void notBookedStatus() {
        // when
        final BookedStatus notBookedStatus = BookedStatus.from(1);

        // then
        assertAll(() -> {
            assertThat(notBookedStatus).isNotNull();
            assertThat(notBookedStatus.getSequence()).isEqualTo(1);
            assertThat(notBookedStatus.isBooked()).isFalse();
        });
    }
} 