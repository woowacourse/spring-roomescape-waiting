package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.reservation.ReservationResponse;

@Sql("/reservation-service-test-data.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CancelReservationServiceTest {

    @Autowired
    ReservationQueryService reservationQueryService;

    @Autowired
    CancelReservationService cancelReservationService;

    @Test
    void 존재하지_않는_id로_삭제할_경우_예외_발생() {
        //given
        List<ReservationResponse> allReservations = reservationQueryService.getAllReservations();
        Long notExistIdToFind = allReservations.size() + 1L;

        //when, then
        assertThatThrownBy(() -> cancelReservationService.deleteReservation(notExistIdToFind))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 예약_삭제_시_예약_대기가_존재한다면_우선순위가_제일_높은_예약_대기를_자동_승인() {
        // given
        List<ReservationResponse> beforeReservations = reservationQueryService.getAllReservations();
        ReservationResponse beforeFirstReservation = beforeReservations.get(0);

        // when
        cancelReservationService.deleteReservation(beforeFirstReservation.id());

        // then
        List<ReservationResponse> afterReservations = reservationQueryService.getAllReservations();
        ReservationResponse afterFirstReservation = afterReservations.get(0);
        assertThat(beforeFirstReservation).isNotEqualTo(afterFirstReservation);
    }

    @Test
    void 예약_삭제_시_예약_대기가_존재하지_않으면_예약_정상_삭제() {
        // given
        List<ReservationResponse> reservations = reservationQueryService.getAllReservations();
        ReservationResponse firstReservations = reservations.get(0);
        cancelReservationService.deleteReservation(firstReservations.id());

        // when
        cancelReservationService.deleteReservation(firstReservations.id());

        // then
        List<ReservationResponse> afterReservations = reservationQueryService.getAllReservations();
        assertThat(afterReservations).isEmpty();
    }
}
