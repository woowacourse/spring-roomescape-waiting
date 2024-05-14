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
import roomescape.dto.reservation.ReservationFilter;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

@Sql("/reservation-service-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ThemeService themeService;

    @Test
    void 잘못된_예약_시간대_id로_예약을_추가할_경우_예외_발생() {
        //given
        List<ReservationTimeResponse> allReservationTimes = reservationTimeService.getAllReservationTimes();
        Long notExistTimeId = allReservationTimes.size() + 1L;

        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now(), notExistTimeId, 1L, 1L);

        //when, then
        assertThatThrownBy(() -> reservationService.addReservation(reservationRequest))
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
        assertThatThrownBy(() -> reservationService.addReservation(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 날짜와_시간대와_테마가_모두_동일한_예약을_추가할_경우_예외_발생() {
        //given
        ReservationRequest reservationRequest1 = new ReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, 1L);
        reservationService.addReservation(reservationRequest1);

        //when, then
        ReservationRequest reservationRequest2 = new ReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, 2L);
        assertThatThrownBy(() -> reservationService.addReservation(reservationRequest2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 지나간_날짜로_예약을_추가할_경우_예외_발생() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().minusDays(1), 1L, 1L, 1L);

        //when, then
        assertThatThrownBy(() -> reservationService.addReservation(reservationRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_id로_조회할_경우_예외_발생() {
        //given
        List<ReservationResponse> allReservations = reservationService.getAllReservations();
        Long notExistIdToFind = allReservations.size() + 1L;

        //when, then
        assertThatThrownBy(() -> reservationService.getReservation(notExistIdToFind))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지_않는_id로_삭제할_경우_예외_발생() {
        //given
        List<ReservationResponse> allReservations = reservationService.getAllReservations();
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
        List<ReservationResponse> reservationResponses = reservationService.getReservationsByFilter(reservationFilter);

        //then
        boolean isAllMatch = reservationResponses.stream()
                .allMatch(response -> response.member().id() == filteringUserId);
        assertThat(isAllMatch).isTrue();
    }

    @Sql("/reservation-filter-api-test-data.sql")
    @Test
    void 특정_테마와_날짜로_필터링_후_예약_조회() {
        //given
        Long filteringThemeId = 1L;
        LocalDate dateFrom = LocalDate.of(2024, 5, 2);
        LocalDate dateTo = LocalDate.of(2024, 5, 3);

        ReservationFilter reservationFilter = new ReservationFilter();
        reservationFilter.setThemeId(filteringThemeId);
        reservationFilter.setDateFrom(dateFrom);
        reservationFilter.setDateTo(dateTo);

        //when
        List<ReservationResponse> reservationResponses = reservationService.getReservationsByFilter(reservationFilter);

        //then
        boolean isAllMatch = reservationResponses.stream()
                .allMatch(response ->
                        response.theme().id() == filteringThemeId &&
                        (response.date().isEqual(dateFrom) || response.date().isAfter(dateFrom)) &&
                        (response.date().isEqual(dateTo) || response.date().isBefore(dateTo))
                );
        assertThat(isAllMatch).isTrue();
    }
}
