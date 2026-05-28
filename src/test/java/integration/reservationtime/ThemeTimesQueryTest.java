package integration.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;

import integration.BaseIntegrationTest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.controller.client.api.dto.response.ThemeTimesResponse;
import roomescape.controller.client.api.query.ThemeTimesQuery;

class ThemeTimesQueryTest extends BaseIntegrationTest {

    @Autowired
    private ThemeTimesQuery themeTimesQuery;
    @Autowired
    private ReservationTimeDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
        dataSource.insertOneTheme();
        dataSource.insertTimeByStartToEndWithOneHourLotation(10, 12);
    }

    @Test
    void 미래_날짜의_테마별_시간_상태를_예약_가능과_대기_가능으로_조회한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        dataSource.insertReservation(1L, date, 1L);

        // when
        List<ThemeTimesResponse> result = themeTimesQuery.getThemeReservationStatus(1L, date);

        // then
        assertThat(result)
                .extracting(ThemeTimesResponse::status)
                .containsExactly("WAITING_AVAILABLE", "RESERVABLE", "RESERVABLE");
    }

    @Test
    void 과거_날짜의_테마별_시간은_마감_상태로_조회한다() {
        // given
        LocalDate date = LocalDate.now().minusDays(1);

        // when
        List<ThemeTimesResponse> result = themeTimesQuery.getThemeReservationStatus(1L, date);

        // then
        assertThat(result)
                .extracting(ThemeTimesResponse::status)
                .containsOnly("UNAVAILABLE");
        assertThat(result)
                .extracting(ThemeTimesResponse::isReservable)
                .containsOnly(false);
    }
}
