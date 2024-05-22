package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import static roomescape.exception.ExceptionType.EMPTY_TIME;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.entity.ReservationTime;
import roomescape.exception.RoomescapeException;

class ReservationTimeTest {

    @DisplayName("시간이 null 인 ReservationTime 을 생성할 수 없다.")
    @Test
    void startAtMustBeNotNull() {
        assertThatThrownBy(() -> new ReservationTime(null))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_TIME.getMessage());
    }

    @DisplayName("ReservationTime 은 id 값으로만 동등성을 비교한다.")
    @Test
    void equalsTest() {
        assertAll(
                () -> assertThat(new ReservationTime(1L, LocalTime.of(11, 11)))
                        .isEqualTo(new ReservationTime(1L, LocalTime.of(11, 20))),

                () -> assertThat(new ReservationTime(1L, LocalTime.of(11, 11)))
                        .isNotEqualTo(new ReservationTime(2L, LocalTime.of(11, 11))),

                () -> assertThat(new ReservationTime(null, LocalTime.of(11, 11)))
                        .isNotEqualTo(new ReservationTime(1L, LocalTime.of(11, 11)))
        );
    }
}
