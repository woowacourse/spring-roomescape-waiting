package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationDate;

class ReservationDateTest {

    @Test
    @DisplayName("LocalDate로 생성하는 경우")
    void reservationDate는_null일_수_없다1() {
        LocalDate date = null;
        assertThatThrownBy(() -> new ReservationDate(date))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("문자열로 생성하는 경우")
    void reservationDate는_null일_수_없다2() {
        String date = null;
        assertThatThrownBy(() -> new ReservationDate(date))
                .isInstanceOf(NullPointerException.class);
    }
}
