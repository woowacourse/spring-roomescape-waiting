package roomescape.unit.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationTime;
import roomescape.exception.ArgumentNullException;

class ReservationTimeTest {

    @Test
    void 시작시간이_null이면_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(() -> ReservationTime.createWithoutId(null))
                .isInstanceOf(ArgumentNullException.class);
    }

    @Test
    void 시작시간이_null이면_예외가_발생한다2() {
        // when & then
        Assertions.assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(ArgumentNullException.class);

    }

}