package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.reservation.ReservationFilter;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;
import roomescape.repository.WaitingRepository;
import roomescape.service.booking.reservation.ReservationService;
import roomescape.service.booking.time.ReservationTimeService;

@Sql("/all-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ThemeService themeService;

    @Autowired
    WaitingRepository waitingRepository;

    @Test
    void 잘못된_예약_시간대_id로_예약을_추가할_경우_예외_발생() {
        //given
        List<ReservationTimeResponse> allReservationTimes = reservationTimeService.findAllReservationTimes();
        Long notExistTimeId = allReservationTimes.size() + 1L;

        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now(), notExistTimeId, 1L, 1L);

        //when, then
        assertThatThrownBy(() -> reservationService.resisterReservation(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 잘못된_테마_id로_예약을_추가할_경우_예외_발생() {
        //given
        List<ThemeResponse> allTheme = themeService.getAllTheme();
        Long notExistIdToFind = allTheme.size() + 1L;

        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now(), 1L, notExistIdToFind, 1L);

        //when, then
        assertThatThrownBy(() -> reservationService.resisterReservation(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 날짜와_시간대와_테마가_모두_동일한_예약을_추가할_경우_예외_발생() {
        //given
        ReservationRequest reservationRequest1 = new ReservationRequest(
                LocalDate.now().plusDays(3), 1L, 1L, 1L);
        reservationService.resisterReservation(reservationRequest1);

        //when, then
        ReservationRequest reservationRequest2 = new ReservationRequest(
                LocalDate.now().plusDays(3), 1L, 1L, 2L);

        assertThatThrownBy(() -> reservationService.resisterReservation(reservationRequest2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 지나간_날짜로_예약을_추가할_경우_예외_발생() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().minusDays(1), 1L, 1L, 1L);

        //when, then
        assertThatThrownBy(() -> reservationService.resisterReservation(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_id로_조회할_경우_예외_발생() {
        //given
        List<ReservationResponse> allReservations = reservationService.findAllReservations();
        Long notExistIdToFind = allReservations.size() + 1L;

        //when, then
        assertThatThrownBy(() -> reservationService.findReservation(notExistIdToFind))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_id로_삭제할_경우_예외_발생() {
        //given
        List<ReservationResponse> allReservations = reservationService.findAllReservations();
        Long notExistIdToFind = allReservations.size() + 1L;

        //when, then
        assertThatThrownBy(() -> reservationService.deleteReservation(notExistIdToFind))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Sql("/reservation-filter-api-test-data.sql")
    @Test
    void 특정_사용자로_필터링_후_예약_조회() {
        //given
        Long filteringUserId = 1L;
        ReservationFilter reservationFilter = new ReservationFilter();
        reservationFilter.setMemberId(filteringUserId);

        //when
        List<ReservationResponse> reservationResponses = reservationService.findReservationsByFilter(reservationFilter);

        //then
        assertThat(reservationResponses).isNotEmpty()
                .allMatch(r -> r.member().id().equals(filteringUserId));
    }

    @Sql("/reservation-filter-api-test-data.sql")
    @Test
    void 특정_테마와_날짜로_필터링_후_예약_조회() {
        //given
        Long filteringThemeId = 1L;
        LocalDate startDate = LocalDate.of(2024, 5, 2);
        LocalDate endDate = LocalDate.of(2024, 5, 3);

        ReservationFilter reservationFilter = new ReservationFilter();
        reservationFilter.setThemeId(filteringThemeId);
        reservationFilter.setStartDate(startDate);
        reservationFilter.setEndDate(endDate);

        //when
        List<ReservationResponse> reservationResponses = reservationService.findReservationsByFilter(reservationFilter);

        //then
        assertThat(reservationResponses).isNotEmpty()
                .allMatch(r ->
                r.theme().id().equals(filteringThemeId) &&
                (r.date().isEqual(startDate) || r.date().isAfter(startDate)) &&
                (r.date().isEqual(endDate) || r.date().isBefore(endDate))
        );
    }

    @Sql("/waiting-test-data.sql")
    @Test
    void 대기_상태의_예약_생성시_자동으로_대기_순번을_지정() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, 4L);

        //when
        Long savedReservationId = reservationService.addReservationWaiting(reservationRequest);

        //then
        Waiting waiting = waitingRepository.findByReservationId(savedReservationId).orElseThrow();

        assertThat(waiting.getWaitingOrderValue()).isEqualTo(3);
    }

    @Sql("/waiting-test-data.sql")
    @Test
    void 대기_상태의_예약_생성시_지나간_날짜로_생성할_경우_예외_발생() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().minusDays(1), 1L, 1L, 4L);

        //when, then
        assertThatThrownBy(() -> reservationService.addReservationWaiting(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Sql("/waiting-test-data.sql")
    @Test
    void 대기_상태의_예약_생성시_사용자에게_이미_동일한_예약이_있을_경우_예외_발생() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, 2L);

        //when, then
        assertThatThrownBy(() -> reservationService.addReservationWaiting(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
