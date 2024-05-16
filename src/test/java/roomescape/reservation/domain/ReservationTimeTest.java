package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

@DisplayName("예약 시간 도메인 테스트")
class ReservationTimeTest {
    @DisplayName("동일한 id는 같은 예약 시간이다.")
    @Test
    void equals() {
        //given
        long id1 = 1;
        LocalTime localTime1 = LocalTime.MIDNIGHT;
        LocalTime localTime2 = LocalTime.MIDNIGHT;

        //when
        ReservationTime reservationTime1 = new ReservationTime(id1, localTime1);
        ReservationTime reservationTime2 = new ReservationTime(id1, localTime2);

        //then
        assertThat(reservationTime1).isEqualTo(reservationTime2);
    }

    @DisplayName("시간 필드에 null을 허용하지 않는다.")
    @Test
    void invalidLocalTime() {
        //given
        long id1 = 1;

        //when & then
        assertThatThrownBy(() -> new ReservationTime(id1, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorType.MISSING_REQUIRED_VALUE_ERROR.getMessage());
    }
}
