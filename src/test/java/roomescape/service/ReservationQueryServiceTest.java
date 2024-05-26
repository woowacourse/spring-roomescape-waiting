package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.reservation.ReservationFilter;
import roomescape.dto.reservation.ReservationResponse;

@Sql("/reservation-service-test-data.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationQueryServiceTest {

    @Autowired
    ReservationQueryService reservationQueryService;

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
        List<ReservationResponse> reservationResponses = reservationQueryService.getReservationsByFilter(reservationFilter);

        //then
        boolean isAllMatch = reservationResponses.stream()
                .allMatch(response ->
                        response.theme().id() == filteringThemeId &&
                        (response.date().isEqual(dateFrom) || response.date().isAfter(dateFrom)) &&
                        (response.date().isEqual(dateTo) || response.date().isBefore(dateTo))
                );
        assertThat(isAllMatch).isTrue();
    }

    @Sql("/reservation-filter-api-test-data.sql")
    @Test
    void 특정_사용자로_필터링_후_예약_조회() {
        //given
        Long filteringUserId = 1L;
        ReservationFilter reservationFilter = new ReservationFilter();
        reservationFilter.setMemberId(filteringUserId);

        //when
        List<ReservationResponse> reservationResponses = reservationQueryService.getReservationsByFilter(reservationFilter);

        //then
        boolean isAllMatch = reservationResponses.stream()
                .allMatch(response -> response.member().id() == filteringUserId);
        assertThat(isAllMatch).isTrue();
    }

    @Test
    void 존재하지_않는_id로_조회할_경우_예외_발생() {
        //given
        List<ReservationResponse> allReservations = reservationQueryService.getAllReservations();
        Long notExistIdToFind = allReservations.size() + 1L;

        //when, then
        assertThatThrownBy(() -> reservationQueryService.getReservation(notExistIdToFind))
                .isInstanceOf(NoSuchElementException.class);
    }
}
