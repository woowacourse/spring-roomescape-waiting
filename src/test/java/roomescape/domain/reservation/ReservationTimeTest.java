package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.system.exception.RoomescapeException;

class ReservationTimeTest {

    @DisplayName("중복 예약 시간은 허용하지 않는다.")
    @Test
    void validateDuplication() {
        // given
        List<ReservationTime> reservationTimes = List.of(new ReservationTime("10:00"));
        ReservationTime reservationTime = new ReservationTime("10:00");
        // when & then
        assertThatCode(() -> reservationTime.validateDuplication(reservationTimes))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 존재하는 시간은 추가할 수 없습니다.");
    }


    @DisplayName("해당 예약 시간을 참조하는 예약이 있으면 삭제할 수 없다.")
    @Test
    void validateHavingReservations() {
        // given
        ReservationTime reservationTime = new ReservationTime("10:00");
        // when & then
        assertThatCode(reservationTime::validateHavingReservations)
            .doesNotThrowAnyException();
    }
}
