package roomescape.reservation.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BadRequestException;
import roomescape.exception.IllegalReservationDateTimeRequestException;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

class ReservationTest {

    /*
    @Test
    @DisplayName("이름이 Null 혹은 빈값인 경우 에러를 발생한다.")
    void validation_ShouldThrowException_WhenNameIsNull() {
        assertAll(() -> {
            Throwable nameIsEmpty = assertThrows(BadRequestException.class,
                    () -> new Reservation(" ", LocalDate.now(), new Time(1L, LocalTime.of(12, 0)), new Theme(1L)));
            assertEquals("공백으로 이루어진 이름으로 예약할 수 없습니다.", nameIsEmpty.getMessage());

            Throwable nameIsNull = assertThrows(BadRequestException.class,
                    () -> new Reservation(null, LocalDate.now(), new Time(1L, LocalTime.of(12, 0)), new Theme(1L)));
            assertEquals("예약 정보가 부족합니다.", nameIsNull.getMessage());
        });
    }

    @Test
    @DisplayName("이름에 특수문자가 들어가는 경우 에러를 발생한다.")
    void validation_ShouldThrowException_WhenNameContainsSymbol() {
        assertAll(() -> {
            Throwable pastDateReservation = assertThrows(BadRequestException.class,
                    () -> new Reservation("@특수문자", LocalDate.now(), new Time(1L, LocalTime.of(12,0)), new Theme(1L)));
            assertEquals("특수문자가 포함된 이름으로 예약을 시도하였습니다.", pastDateReservation.getMessage());
        });
    }
*/
}
