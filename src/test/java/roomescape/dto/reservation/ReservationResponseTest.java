package roomescape.dto.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservation.Reservation;
import roomescape.repository.ReservationRepository;

@Sql("/all-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationResponseTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    void 예약_응답_생성() {
        //given
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();

        //when
        ReservationResponse reservationResponse = ReservationResponse.from(reservation);

        //then
        assertAll(
                () -> assertThat(reservationResponse.id()).isEqualTo(reservation.getId()),
                () -> assertThat(reservationResponse.date()).isEqualTo(reservation.getDate()),
                () -> assertThat(reservationResponse.time().id()).isEqualTo(reservation.getTime().getId()),
                () -> assertThat(reservationResponse.theme().id()).isEqualTo(reservation.getTheme().getId()),
                () -> assertThat(reservationResponse.member().id()).isEqualTo(reservation.getMember().getId())
        );
    }
}
