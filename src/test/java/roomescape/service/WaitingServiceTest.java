package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Status;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Sql("/waiting-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingServiceTest {

    @Autowired
    WaitingService waitingService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    WaitingRepository waitingRepository;

    @Test
    void 모든_대기중인_예약_조회() {
        //given, when
        List<WaitingResponse> allWaitingReservations = waitingService.getAllWaitingReservations();

        //then
        assertThat(allWaitingReservations).hasSize(2);
    }

    @Test
    void 예약_대기_삭제후_자동으로_다음_대기번호_변경() {
        //given
        Waiting waiting1 = waitingRepository.findById(1L).orElseThrow();
        int waitingOrder1 = waiting1.getWaitingOrderValue();

        Waiting waiting2 = waitingRepository.findById(2L).orElseThrow();
        int waitingOrder2 = waiting2.getWaitingOrderValue();

        //when
        waitingService.deleteWaiting(1L);

        //then
        List<WaitingResponse> allWaitingReservations = waitingService.getAllWaitingReservations();

        assertAll(
                () -> assertThat(allWaitingReservations).extracting(WaitingResponse::waitingId).containsOnly(2L),
                () -> assertThat(waitingOrder2).isEqualTo(2),
                () -> assertThat(waiting2.getWaitingOrderValue()).isEqualTo(waitingOrder1)
        );
    }

    @Test
    void 사용자는_예약_id를_통해_예약_대기_삭제후_자동으로_다음_대기번호_변경() {
        //given
        Waiting waiting1 = waitingRepository.findById(1L).orElseThrow();
        Reservation reservation = waiting1.getReservation();
        int waitingOrder1 = waiting1.getWaitingOrderValue();

        Waiting waiting2 = waitingRepository.findById(2L).orElseThrow();
        int waitingOrder2 = waiting2.getWaitingOrderValue();

        //when
        waitingService.deleteWaitingForUser(reservation.getId());

        //then
        List<WaitingResponse> allWaitingReservations = waitingService.getAllWaitingReservations();

        assertAll(
                () -> assertThat(allWaitingReservations).extracting(WaitingResponse::waitingId).containsOnly(2L),
                () -> assertThat(waitingOrder2).isEqualTo(2),
                () -> assertThat(waiting2.getWaitingOrderValue()).isEqualTo(waitingOrder1)
        );
    }

    @Test
    void 예약이_확정된_상태의_예약을_취소할_경우_예외_발생() {
        //given
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();

        //when, then
        assertAll(
                () -> assertThat(reservation.getStatus()).isEqualTo(Status.RESERVED),
                () -> assertThatThrownBy(() -> waitingService.deleteWaitingForUser(reservation.getId()))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }
}
