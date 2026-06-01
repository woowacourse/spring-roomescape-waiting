package roomescape.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.service.result.ThemeTimesResult;
import roomescape.support.BaseIntegrationTest;
import roomescape.support.ReservationTimeDataSource;

class ThemeTimesQueryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ThemeQueryRepository themeQueryRepository;
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
        List<ThemeTimesResult> result = themeQueryRepository.getThemeReservationStatus(1L, date);

        // then
        assertThat(result)
                .extracting(ThemeTimesResult::status)
                .containsExactly("WAITING_AVAILABLE", "RESERVABLE", "RESERVABLE");
    }

    @Test
    void 과거_날짜의_테마별_시간은_마감_상태로_조회한다() {
        // given
        LocalDate date = LocalDate.now().minusDays(1);

        // when
        List<ThemeTimesResult> result = themeQueryRepository.getThemeReservationStatus(1L, date);

        // then
        assertThat(result)
                .extracting(ThemeTimesResult::status)
                .containsOnly("UNAVAILABLE");
        assertThat(result)
                .extracting(ThemeTimesResult::isReservable)
                .containsOnly(false);
    }
}
