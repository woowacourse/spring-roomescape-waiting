package roomescape.domain.reservation;

import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {
    @Test
    void null을_입력받으면_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> ReservationTime.of(1, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 유효한_요청이면_생성에_성공한다() {
        Assertions.assertThatNoException().isThrownBy(() -> ReservationTime.of(LocalTime.now()));
    }
}
