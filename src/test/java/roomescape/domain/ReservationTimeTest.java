package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidDomainException;

class ReservationTimeTest {

    @Test
    void 시작_시간이_null이면_예외() {
        assertThatThrownBy(() -> new ReservationTime(null, null))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("예약 시간은 필수입니다.");
    }
}