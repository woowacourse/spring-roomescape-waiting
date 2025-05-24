package roomescape.reservationtime.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

public class ReservationTimeTest {

    @Test
    void 생성_성공() {
        // given
        LocalTime startAt = LocalTime.of(13, 0);

        // when
        ReservationTime time = ReservationTime.from(startAt);

        // then
        Assertions.assertThat(time.getStartAt()).isEqualTo(startAt);
    }

    @Test
    void null_값_입력_시_NullPointerException_발생() {
        // when & then
        Assertions.assertThatThrownBy(() -> ReservationTime.from(null))
                .isInstanceOf(NullPointerException.class);
    }
}
