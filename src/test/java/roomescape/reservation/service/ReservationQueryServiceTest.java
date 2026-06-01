package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.fixture.ThemeFixture;
import roomescape.reservation.application.dto.ReservationPageResult;
import roomescape.reservation.application.dto.ReservationResult;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;

@ServiceTest
@Transactional
class ReservationQueryServiceTest {

    @Autowired
    private ReservationQueryService reservationQueryService;

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("예약 전체 조회를 테스트합니다.")
    @Test
    void find_all_reservations() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        LocalDate earlierDate = LocalDate.of(2026, 5, 6);
        LocalDate laterDate = LocalDate.of(2026, 5, 7);

        testHelper.insertReservation(
                "스타크",
                earlierDate,
                themeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "비밥",
                laterDate,
                themeId,
                tenTimeId
        );

        ReservationPageResult page = reservationQueryService.findAllByPage(0);
        List<ReservationResult> reservations = page.content();

        ReservationResult first = reservations.getFirst();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(2);
            softly.assertThat(page.page()).isZero();
            softly.assertThat(page.size()).isEqualTo(20);
            softly.assertThat(page.totalElements()).isEqualTo(2);
            softly.assertThat(page.totalPages()).isEqualTo(1);
            softly.assertThat(first.id()).isPositive();
            softly.assertThat(first.name()).isEqualTo("스타크");
            softly.assertThat(first.date()).isEqualTo(earlierDate);
            softly.assertThat(first.theme()).isEqualTo(ThemeFixture.horrorThemeQueryResult(themeId));
            softly.assertThat(first.time()).isEqualTo(new ReservationTimeResult(nineTimeId, LocalTime.of(9, 0)));
        });
    }

    @DisplayName("사용자의 이름으로 예약 목록 조회를 테스트 합니다.")
    @Test
    void find_reservations_by_name() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        LocalDate earlierDate = LocalDate.of(2026, 5, 6);
        LocalDate laterDate = LocalDate.of(2026, 5, 7);

        testHelper.insertReservation(
                "스타크",
                earlierDate,
                themeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "비밥",
                laterDate,
                themeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "스타크",
                laterDate,
                themeId,
                tenTimeId
        );

        List<ReservationResult> reservations = reservationQueryService.findByName("스타크");

        ReservationResult first = reservations.getFirst();
        ReservationResult second = reservations.get(1);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(2);
            softly.assertThat(first.name()).isEqualTo("스타크");
            softly.assertThat(first.date()).isEqualTo(earlierDate);
            softly.assertThat(first.theme()).isEqualTo(ThemeFixture.horrorThemeQueryResult(themeId));
            softly.assertThat(first.time()).isEqualTo(new ReservationTimeResult(nineTimeId, LocalTime.of(9, 0)));
            softly.assertThat(second.name()).isEqualTo("스타크");
            softly.assertThat(second.date()).isEqualTo(laterDate);
            softly.assertThat(second.theme()).isEqualTo(ThemeFixture.horrorThemeQueryResult(themeId));
            softly.assertThat(second.time()).isEqualTo(new ReservationTimeResult(tenTimeId, LocalTime.of(10, 0)));
        });
    }

}
