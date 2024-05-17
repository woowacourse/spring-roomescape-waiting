package roomescape.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {
    @Test
    @DisplayName("예약 시간을 저장할 때, 문자열을 LocalTime 타입으로 변환한다.")
    void parseStartAt() {
        final String startAt = "10:00";
        final ReservationTime reservationTime = new ReservationTime(startAt);

        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("예약 시간 형식이 올바르지 않을 경우 예외가 발생한다.")
    void parseStartAtWithInvalidFormat() {
        final String startAt = "abcdefgh";

        assertThatThrownBy(() -> new ReservationTime(startAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시간 형식이 잘못되었습니다.");
    }

    @Test
    @DisplayName("예약 시간이 현재 시간보다 이전인지 확인할 수 있다.")
    void isPast() {
        final String startAt = LocalTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm"));
        final ReservationTime reservationTime = new ReservationTime(startAt);

        final boolean isPast = reservationTime.isPast();
        assertThat(isPast).isTrue();
    }
}
