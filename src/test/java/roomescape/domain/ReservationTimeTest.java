package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

class ReservationTimeTest {

    @DisplayName("등록 시간은 null 값을 넣으면 예외가 발생한다.")
    @Test
    void given_when_newWithNull_then_throwException() {
        //given, when, then
        assertThatThrownBy(() -> new ReservationTime(1L, (LocalTime) null)).isInstanceOf(
                EmptyValueNotAllowedException.class);
    }
}
