package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.ThemeFixture;
import roomescape.reservationtime.application.dto.AvailableReservationTimeResult;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.reservationtime.application.service.ReservationTimeQueryService;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;

@ServiceTest
public class ReservationTimeQueryServiceTest {

    @Autowired
    private ReservationTimeQueryService timeQueryService;

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("예약 시간의 전체 조회를 테스트합니다.")
    @Test
    void find_all_times() {

        Long nineId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long elevenId = testHelper.insertReservationTime(LocalTime.of(11, 0));

        List<ReservationTimeResult> times = timeQueryService.findAll();

        assertThat(times).containsExactly(
                new ReservationTimeResult(nineId, LocalTime.of(9, 0)),
                new ReservationTimeResult(tenId, LocalTime.of(10, 0)),
                new ReservationTimeResult(elevenId, LocalTime.of(11, 0))
        );
    }

    @DisplayName("날짜/테마 선택 시 예약 가능한 시간 조회를 테스트합니다.")
    @Test
    void find_available_times_by_date_and_theme() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());

        Long nineId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long elevenId = testHelper.insertReservationTime(LocalTime.of(11, 0));

        LocalDate reservationDate = LocalDate.of(2026, 5, 6);
        testHelper.insertReservation(
                "스타크",
                reservationDate,
                themeId,
                nineId
        );

        List<AvailableReservationTimeResult> times = timeQueryService.findAvailableTimes(themeId, reservationDate);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(times).hasSize(3);
            softly.assertThat(times).containsExactly(
                    new AvailableReservationTimeResult(nineId, LocalTime.of(9, 0), false),
                    new AvailableReservationTimeResult(tenId, LocalTime.of(10, 0), true),
                    new AvailableReservationTimeResult(elevenId, LocalTime.of(11, 0), true)
            );
        });
    }
}
