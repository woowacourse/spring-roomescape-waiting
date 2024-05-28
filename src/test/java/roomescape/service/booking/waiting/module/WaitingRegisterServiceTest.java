package roomescape.service.booking.waiting.module;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Status;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Sql("/all-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingRegisterServiceTest {

    @Autowired
    WaitingRegisterService waitingRegisterService;

    @Autowired
    WaitingRepository waitingRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Sql("/waiting-test-data.sql")
    @Test
    void 대기_예약_등록시_자동으로_대기_순번을_지정하여_등록() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, 4L);

        //when
        Long reservationId = waitingRegisterService.registerWaiting(reservationRequest);

        //then
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        Waiting waiting = waitingRepository.findByReservationId(reservationId).orElseThrow();

        assertAll(
                () -> assertThat(reservation.getId()).isEqualTo(reservationId),
                () -> assertThat(reservation.getDate()).isEqualTo(reservationRequest.date()),
                () -> assertThat(reservation.getTime().getId()).isEqualTo(reservationRequest.timeId()),
                () -> assertThat(reservation.getMember().getId()).isEqualTo(reservationRequest.memberId()),
                () -> assertThat(reservation.getStatus()).isEqualTo(Status.WAITING),
                () -> assertThat(waiting.getWaitingOrderValue()).isEqualTo(3)
        );
    }

    @Sql("/waiting-test-data.sql")
    @Test
    void 대기_상태의_예약_등록시_지나간_날짜로_등록할_경우_예외_발생() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().minusDays(1), 1L, 1L, 4L);

        //when, then
        assertThatThrownBy(() -> waitingRegisterService.registerWaiting(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Sql("/waiting-test-data.sql")
    @Test
    void 대기_상태의_예약_등록시_사용자에게_이미_동일한_예약이_있을_경우_예외_발생() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, 2L);

        //when, then
        assertThatThrownBy(() -> waitingRegisterService.registerWaiting(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
