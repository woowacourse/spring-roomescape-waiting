package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    @DisplayName("성공적으로 시간 도메인 객체를 생성한다.")
    void ofSuccess() {
        // given
        LocalTime time = LocalTime.of(10, 0);

        // when
        ReservationTime reservationTime = ReservationTime.of(time);

        // then
        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reservationTime.getId()).isNull();
    }

    @Test
    @DisplayName("생성된 시간 객체의 필드 값을 확인한다.")
    void getterTest() {
        // given
        LocalTime now = LocalTime.now();
        ReservationTime reservationTime = new ReservationTime(1L, now);

        // then
        assertThat(reservationTime.getId()).isEqualTo(1L);
        assertThat(reservationTime.getStartAt()).isEqualTo(now);
    }
}
