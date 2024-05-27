package roomescape.service.booking.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

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
import roomescape.dto.reservation.ReservationResponse;
import roomescape.service.booking.reservation.module.ReservationSearchService;

@Sql("/all-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationSearchServiceTest {

    @Autowired
    ReservationSearchService reservationSearchService;

    @Test
    void 단일_예약_조회() {
        //when
        ReservationResponse reservationResponse = reservationSearchService.findReservation(1L);

        //then
        assertAll(
                () -> assertThat(reservationResponse.id()).isEqualTo(1L),
                () -> assertThat(reservationResponse.date()).isEqualTo(LocalDate.now().minusDays(1)),
                () -> assertThat(reservationResponse.time().id()).isEqualTo(1L),
                () -> assertThat(reservationResponse.theme().id()).isEqualTo(1L),
                () -> assertThat(reservationResponse.member().id()).isEqualTo(1L)
        );
    }

    @Test
    void 전체_예약_조회() {
        //when
        List<ReservationResponse> allReservationResponses = reservationSearchService.findAllReservations();

        //then
        assertThat(allReservationResponses).hasSize(4);
    }

    @Sql("/reservation-filter-api-test-data.sql")
    @Test
    void 특정_사용자로_필터링_후_예약_조회() {
        //given
        Long filteringUserId = 1L;
        ReservationFilter reservationFilter = new ReservationFilter();
        reservationFilter.setMemberId(filteringUserId);

        //when
        List<ReservationResponse> reservationResponses = reservationSearchService.findReservationsByFilter(
                reservationFilter);

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
        List<ReservationResponse> reservationResponses = reservationSearchService.findReservationsByFilter(
                reservationFilter);

        //then
        assertThat(reservationResponses).isNotEmpty()
                .allMatch(r ->
                        r.theme().id().equals(filteringThemeId) &&
                                (r.date().isEqual(startDate) || r.date().isAfter(startDate)) &&
                                (r.date().isEqual(endDate) || r.date().isBefore(endDate))
                );
    }

    @Test
    void 존재하지_않는_id로_조회할_경우_예외_발생() {
        //given
        Long notExistIdToFind = reservationSearchService.findAllReservations().size() + 1L;

        //when, then
        assertThatThrownBy(() -> reservationSearchService.findReservation(notExistIdToFind))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
