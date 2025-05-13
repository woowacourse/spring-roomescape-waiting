package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @DisplayName("같은 시간 여부를 검증한다.")
    @Test
    void isSameTime() {
        //given
        ReservationTime time = ReservationTime.parse("10:30");
        ReservationTime sameTime = ReservationTime.parse("10:30");
        ReservationTime anotherTime = ReservationTime.parse("10:31");

        //when
        boolean isSameTime = time.isSameTime(sameTime);
        boolean isNotSameTime = sameTime.isSameTime(anotherTime);

        //then
        assertThat(isSameTime).isTrue();
        assertThat(isNotSameTime).isFalse();
    }
}
