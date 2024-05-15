package roomescape.reservation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.dto.MemberNameResponse;
import roomescape.theme.dto.ThemeResponse;

import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.InitialDataFixture.RESERVATION_1;

class ReservationResponseTest {

    @Test
    @DisplayName("Reservation 객체로 ReservationResponse를 만든다.")
    void createReservationResponse() {
        ReservationResponse expected = new ReservationResponse(
                RESERVATION_1.getId(),
                RESERVATION_1.getMember().getName().name(),
                RESERVATION_1.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                new ReservationTimeResponse(RESERVATION_1.getTime()),
                new ThemeResponse(RESERVATION_1.getTheme()),
                new MemberNameResponse(RESERVATION_1.getMember())
        );

        ReservationResponse reservationResponse = new ReservationResponse(RESERVATION_1);

        assertThat(reservationResponse).isEqualTo(expected);
    }
}
