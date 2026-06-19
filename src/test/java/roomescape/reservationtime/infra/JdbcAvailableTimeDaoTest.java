package roomescape.reservationtime.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.fixture.ThemeFixture;
import roomescape.reservationtime.application.dao.AvailableTimeDao;
import roomescape.reservationtime.application.dto.AvailableReservationTimeResult;
import roomescape.support.TestDataHelper;

@JdbcTest
@Import(JdbcAvailableTimeDao.class)
class JdbcAvailableTimeDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AvailableTimeDao availableTimeDao;

    private TestDataHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TestDataHelper(jdbcTemplate);
    }

    @DisplayName("예약이 없을 때 모든 예약 시간이 예약 가능으로 조회되는 것을 테스트합니다.")
    @Test
    void find_available_times_without_reservation() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long elevenTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));

        List<AvailableReservationTimeResult> times = availableTimeDao.findByThemeAndDate(
                themeId,
                LocalDate.of(2026, 5, 6)
        );

        assertThat(times).containsExactly(
                new AvailableReservationTimeResult(nineTimeId, LocalTime.of(9, 0), true),
                new AvailableReservationTimeResult(tenTimeId, LocalTime.of(10, 0), true),
                new AvailableReservationTimeResult(elevenTimeId, LocalTime.of(11, 0), true)
        );
    }

    @DisplayName("선택한 날짜와 테마에 예약된 시간은 예약 불가로 조회되는 것을 테스트합니다.")
    @Test
    void find_available_times_with_reserved_time() {
        Long themeId = testHelper.insertTheme(ThemeFixture.themeCreateCommand(1));
        Long otherThemeId = testHelper.insertTheme(ThemeFixture.themeCreateCommand(2));
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long elevenTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
        LocalDate reservationDate = LocalDate.of(2026, 5, 6);

        testHelper.insertReservation(
                "예약자",
                reservationDate,
                themeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "다른 테마 예약자",
                reservationDate,
                otherThemeId,
                tenTimeId
        );
        testHelper.insertReservation(
                "다른 날짜 예약자",
                reservationDate.plusDays(1),
                themeId,
                elevenTimeId
        );

        List<AvailableReservationTimeResult> times = availableTimeDao.findByThemeAndDate(themeId, reservationDate);

        assertThat(times).containsExactly(
                new AvailableReservationTimeResult(nineTimeId, LocalTime.of(9, 0), false),
                new AvailableReservationTimeResult(tenTimeId, LocalTime.of(10, 0), true),
                new AvailableReservationTimeResult(elevenTimeId, LocalTime.of(11, 0), true)
        );
    }
}
