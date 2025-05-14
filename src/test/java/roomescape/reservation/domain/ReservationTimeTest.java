package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReservationTimeTest {

    @DisplayName("id 존재를 확인할 수 있다.")
    @ParameterizedTest
    @CsvSource(value = {"1,true", "0,false"})
    void test1(long id, boolean expect) {
        LocalTime now = LocalTime.now();

        ReservationTime reservationTime = new ReservationTime(id, now);
        assertThat(reservationTime.existId()).isEqualTo(expect);
    }

    @DisplayName("시간은 null일 수 없다.")
    @Test
    void test2() {
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalArgumentException.class);

        softAssertions.assertThatThrownBy(() -> ReservationTime.withoutId(null))
                .isInstanceOf(IllegalArgumentException.class);

        softAssertions.assertAll();
    }
}
