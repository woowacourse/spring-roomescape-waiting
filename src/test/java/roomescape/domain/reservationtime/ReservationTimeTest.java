package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.support.exception.RoomescapeException;

class ReservationTimeTest {

    @Test
    void id가_없는_예약_시간을_생성한다() {
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
    void 예약_시간을_HH_mm_형식으로_반환한다() {
        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));

        // when
        String formattedStartAt = reservationTime.getFormattedStartAt();

        // then
        assertThat(formattedStartAt).isEqualTo("10:00");
    }

    @Test
    void 예약_시간이_null이면_예외가_발생한다() {
        // given
        LocalTime startAt = null;

        // when & then
        assertThatThrownBy(() -> ReservationTime.createWithoutId(startAt))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("예약 시간 식별자 정보가 누락되었습니다.");
    }
}
