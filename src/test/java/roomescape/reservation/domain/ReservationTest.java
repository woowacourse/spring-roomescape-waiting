package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.InitialDataFixture.RESERVATION_1;
import static roomescape.InitialDataFixture.RESERVATION_2;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_1;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    @DisplayName("입력받은 formatter에 맞게 날짜를 String으로 변환한다.")
    void formatGetDate() {
        Reservation reservation = new Reservation(
                LocalDate.of(2024, 4, 24),
                null,
                null,
                LOGIN_MEMBER_1
        );

        String formatted = reservation.getDate(DateTimeFormatter.ISO_DATE);

        assertThat(formatted).isEqualTo("2024-04-24");
    }

    @Test
    @DisplayName("Reservation 객체의 동등성을 따질 때는 id만 확인한다.")
    void testEquals() {
        Reservation reservation = new Reservation(
                RESERVATION_1.getId(),
                RESERVATION_2.getDate(),
                RESERVATION_2.getTime(),
                RESERVATION_2.getTheme(),
                LOGIN_MEMBER_1
        );

        assertThat(RESERVATION_1).isEqualTo(reservation);
    }
}
