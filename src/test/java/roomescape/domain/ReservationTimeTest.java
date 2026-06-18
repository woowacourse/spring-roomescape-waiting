package roomescape.domain;

import java.time.LocalTime;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationtime.ReservationTime;

class ReservationTimeTest {

    @Test
    @DisplayName("정상 예약 시간 생성")
    void createNew_Success() {
        // given
        LocalTime time = LocalTime.parse("10:00");

        // when
        ReservationTime reservationTime = ReservationTime.createNew(time);

        // then
        assertThat(reservationTime.getId()).isNull();
        assertThat(reservationTime.getStartAt()).isEqualTo(time);
    }

    @Test
    @DisplayName("예약 시간 null 예외")
    void validate_NullStartAt_ThrowsException() {
        // given
        LocalTime nullTime = null;

        // when & then
        assertThatThrownBy(() -> ReservationTime.createNew(nullTime))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
