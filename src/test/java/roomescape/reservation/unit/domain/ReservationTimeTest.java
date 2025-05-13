package roomescape.reservation.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.reservation.entity.ReservationTime;

class ReservationTimeTest {

    @Test
    @DisplayName("예약 시간이 중복되는지 확인한다.")
    void isDuplicatedWith() {
        // given
        var time1 = new ReservationTime(1L, LocalTime.of(10, 0));
        var time2 = new ReservationTime(2L, LocalTime.of(11, 0));
        var time3 = new ReservationTime(3L, LocalTime.of(12, 0));

        // when & then
        assertThat(time1.isDuplicatedWith(time2)).isTrue();
        assertThat(time1.isDuplicatedWith(time3)).isFalse();
    }

    @ParameterizedTest
    @DisplayName("예약 시간이 운영 시간 내에 있는지 확인한다.")
    @CsvSource({
            "09:00, false",  // 운영 시작 시간 이전
            "10:00, true",   // 운영 시작 시간
            "15:00, true",   // 운영 시간 내
            "22:00, true",   // 운영 종료 시간
            "23:00, false"   // 운영 종료 시간 이후
    })
    void isAvailable(String time, boolean expected) {
        // given
        var reservationTime = new ReservationTime(1L, LocalTime.parse(time));

        // when
        var result = reservationTime.isAvailable();

        // then
        assertThat(result).isEqualTo(expected);
    }
} 