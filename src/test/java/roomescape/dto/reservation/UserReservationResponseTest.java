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
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Sql("/waiting-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserReservationResponseTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    WaitingRepository waitingRepository;

    @Test
    void 예약_상태의_사용자_예약_응답을_생성() {
        //given
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();

        //when
        UserReservationResponse userReservationResponse = UserReservationResponse.create(reservation);

        //then
        assertAll(
                () -> assertThat(userReservationResponse.id()).isEqualTo(reservation.getId()),
                () -> assertThat(userReservationResponse.theme()).isEqualTo(reservation.getTheme().getThemeName()),
                () -> assertThat(userReservationResponse.date()).isEqualTo(reservation.getDate()),
                () -> assertThat(userReservationResponse.time()).isEqualTo(reservation.getTime().getStartAt()),
                () -> assertThat(userReservationResponse.status()).isEqualTo("예약")
        );
    }

    @Test
    @Transactional
    void 예약_대가_상태의_사용자_예약_응답을_생성() {
        //given
        Waiting waiting = waitingRepository.findById(1L).orElseThrow();
        Reservation reservation = waiting.getReservation();

        //when
        UserReservationResponse userReservationResponse = UserReservationResponse.createByWaiting(waiting);

        //then
        String orderWaiting = String.format("%d번째 예약 대기", waiting.getWaitingOrderValue());
        assertAll(
                () -> assertThat(userReservationResponse.id()).isEqualTo(reservation.getId()),
                () -> assertThat(userReservationResponse.theme()).isEqualTo(reservation.getTheme().getThemeName()),
                () -> assertThat(userReservationResponse.date()).isEqualTo(reservation.getDate()),
                () -> assertThat(userReservationResponse.time()).isEqualTo(reservation.getTime().getStartAt()),
                () -> assertThat(userReservationResponse.status()).isEqualTo(orderWaiting)
        );
    }
}
