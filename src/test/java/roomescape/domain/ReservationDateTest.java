package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationDate;

class ReservationDateTest {

    @Test
    @DisplayName("날짜가 같으면 같은 hash 값을 갖고 동일한 객체이다.")
    void hash_equals_override_test() {
        ReservationDate date1 = new ReservationDate("2024-04-21");
        ReservationDate date2 = new ReservationDate("2024-04-21");

        assertAll(
                () -> assertThat(date1.hashCode()).isEqualTo(date2.hashCode()),
                () -> assertThat(date1).isEqualTo(date2)
        );
    }
}
