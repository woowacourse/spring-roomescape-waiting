package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;

@Sql("/reservation-time-service-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTimeServiceTest {

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ReservationService reservationService;

    @Test
    void 동일한_시간을_추가할_경우_예외_발생() {
        //given
        ReservationTimeRequest reservationTimeRequest1 = new ReservationTimeRequest(LocalTime.parse("00:00"));
        reservationTimeService.addReservationTime(reservationTimeRequest1);

        //when, then
        ReservationTimeRequest reservationTimeRequest2 = new ReservationTimeRequest(LocalTime.parse("00:00"));
        assertThatThrownBy(() -> reservationTimeService.addReservationTime(reservationTimeRequest2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_id로_조회할_경우_예외_발생() {
        //given
        List<ReservationTimeResponse> allReservationTimes = reservationTimeService.getAllReservationTimes();
        Long notExistIdToFind = allReservationTimes.size() + 1L;

        //when, then
        assertThatThrownBy(() -> reservationTimeService.getReservationTime(notExistIdToFind))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약이_존재하는_시간대를_삭제할_경우_예외_발생() {
        //given
        ReservationResponse reservationResponse = reservationService.getReservation(1L);
        ReservationTimeResponse timeResponse = reservationResponse.time();
        Long timeId = timeResponse.id();

        //when, then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(timeId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 특정_날짜와_테마에_예약이_없는_시간대를_조회() {
        //given, when
        List<ReservationTimeResponse> availableTimes = reservationTimeService.getAvailableTimes(
                LocalDate.now().plusDays(1),
                1L
        );

        List<Long> availableTimeIds = availableTimes.stream()
                .map(ReservationTimeResponse::id)
                .toList();

        //then
        assertThat(availableTimeIds).containsExactlyInAnyOrder(3L, 5L);
    }
}
