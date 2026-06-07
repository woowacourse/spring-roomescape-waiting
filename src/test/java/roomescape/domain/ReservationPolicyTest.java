package roomescape.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class ReservationPolicyTest {

    @Test
    void getReservableDates_오늘부터_14일_반환() {
        // when
        List<LocalDate> dates = ReservationPolicy.getReservableDates();

        // then
        Assertions.assertThat(dates).hasSize(14);
        Assertions.assertThat(dates.getFirst()).isEqualTo(LocalDate.now());
        Assertions.assertThat(dates.getLast()).isEqualTo(LocalDate.now().plusDays(13));
    }
}
