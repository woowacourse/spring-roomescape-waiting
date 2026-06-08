package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("예약 시간")
class ReservationTimeTest {

    @Test
    @DisplayName("생성하면 식별자 없이 시작 시간을 담는다")
    void create() {
        // when
        ReservationTime time = ReservationTime.create(LocalTime.of(18, 30));

        // then
        assertThat(time.getId()).isNull();
        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(18, 30));
    }

    @Test
    @DisplayName("조회 결과를 그대로 담는다")
    void of() {
        // when
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));

        // then
        assertThat(time.getId()).isEqualTo(1L);
        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("기준 시간보다 이른 시간인지 확인할 수 있다")
    void isBefore() {
        // given
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));

        // when & then
        assertThat(time.isBefore(LocalTime.of(10, 30))).isTrue();
        assertThat(time.isBefore(LocalTime.of(10, 0))).isFalse();
        assertThat(time.isBefore(LocalTime.of(9, 59))).isFalse();
    }
}
