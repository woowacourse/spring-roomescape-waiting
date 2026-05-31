package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;

class ReservationTimeTest {

    @DisplayName("예약 시간을 저장한다.")
    @Test
    void create() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

        assertThat(reservationTime.getId()).isEqualTo(1L);
        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("ID가 null이어도 아직 저장 전 도메인으로 생성할 수 있다.")
    @Test
    void nullId() {
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(10, 0));

        assertThat(reservationTime.getId()).isNull();
    }

    @DisplayName("시작 시각은 null일 수 없다.")
    @Test
    void nullStartAt() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.INVALID_INPUT);
    }
}
