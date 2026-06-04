package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.RoomescapeException;

class ReservationTimeTest {

    @Test
    @DisplayName("id가 없는 예약 시간을 생성한다.")
    void createReservationTimeWithoutId() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);

        // when
        ReservationTime reservationTime = ReservationTime.createWithoutId(startAt);

        // then
        assertSoftly(softly -> {
            softly.assertThat(reservationTime.getId()).isNull();
            softly.assertThat(reservationTime.getStartAt()).isEqualTo(startAt);
        });
    }

    @Test
    @DisplayName("예약 시간이 null이면 예외가 발생한다.")
    void throwExceptionWhenReservationTimeIsNull() {
        // given
        LocalTime startAt = null;

        // when & then
        assertThatThrownBy(() -> ReservationTime.createWithoutId(startAt))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("시간은 필수입니다.");
    }
}
