package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exceptions.ValidationException;

class ReservationTimeTest {

    @Test
    @DisplayName("입력받은 formatter에 맞게 시간을 String으로 변환한다.")
    void formatGetTime() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));

        String formatted = reservationTime.getStartAt(DateTimeFormatter.ofPattern("HH:mm"));

        assertThat(formatted).isEqualTo("10:00");
    }

    @Test
    @DisplayName("ReservationTime 객체의 동등성을 따질 때는 id만 확인한다.")
    void testEquals() {
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime reservationTime2 = new ReservationTime(1L, LocalTime.of(19, 30));

        assertThat(reservationTime1).isEqualTo(reservationTime2);
    }

    @Test
    @DisplayName("startAt이 Null인 경우 예외가 발생한다.")
    void getStartAtCannotBeNull() {
        LocalTime startAt = null;

        assertThatThrownBy(() -> new ReservationTime(1L, startAt))
                .isInstanceOf(ValidationException.class);
    }
}
