package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InactiveException;
import roomescape.common.exception.ValidationException;
import roomescape.time.domain.ReservationTime;

class ReservationTimeTest {

    @Test
    void 정상적인_예약_시간을_생성한다() {
        // when
        ReservationTime time = ReservationTime.create(LocalTime.of(10, 0));

        // then
        assertThat(time).extracting(ReservationTime::getStartAt, ReservationTime::isActive)
                .containsExactly(LocalTime.of(10, 0), true);
    }

    @Test
    void 예약_시간이_없으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReservationTime.create(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void 비활성화된_예약_시간을_검증하면_예외가_발생한다() {
        // given
        ReservationTime time = ReservationTime.create(LocalTime.of(10, 0)).deactivate();

        // when & then
        assertThatThrownBy(time::validateInactive).isInstanceOf(InactiveException.class);
    }
}
