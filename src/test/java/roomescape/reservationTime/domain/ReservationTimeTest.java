package roomescape.reservationTime.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReservationTimeTest {

    @ParameterizedTest
    @DisplayName("같은 시간 확인 테스트")
    @CsvSource({"20:10,true", "20:20,false", "21:10,false", "19:09,false"})
    void isSameTime_Test(LocalTime localTime, boolean expected) {
        // given
        ReservationTime reservationTime1 = ReservationTime.createWithId(1L, LocalTime.of(20, 10));
        ReservationTime reservationTime2 = ReservationTime.createWithId(2L, localTime);
        // when & then
        assertThat(reservationTime1.isSameTime(reservationTime2)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Id 할당 테스트")
    void assignId_Test() {
        // given
        ReservationTime withoutId = ReservationTime.createWithoutId(LocalTime.of(10, 10));
        // when
        ReservationTime reservationTime = withoutId.assignId(1L);
        // then
        assertThat(reservationTime.getId()).isEqualTo(1L);
        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 10));
    }

    @ParameterizedTest
    @CsvSource({"09:00,false", "09:59,false", "10:01,true", "11:00,true"})
    @DisplayName("이전 시간 확인 테스트")
    void is_before_time_test(LocalTime time, boolean expected) {
        // given
        ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10, 0));
        // when
        boolean beforeTime = reservationTime.isBeforeTime(time);
        // then
        assertThat(beforeTime).isEqualTo(expected);
    }
}
