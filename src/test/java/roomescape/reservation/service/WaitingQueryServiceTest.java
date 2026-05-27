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
import roomescape.reservation.application.dto.ReservationResult;
import roomescape.reservation.application.dto.ReservationResult.Status;
import roomescape.reservation.application.dto.ReservationSearchCondition;
import roomescape.reservation.application.service.WaitingQueryService;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;

@ServiceTest
@Transactional
class WaitingQueryServiceTest {

    @Autowired
    private WaitingQueryService waitingQueryService;

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("특정 이름으로 예약 대기 조회를 테스트합니다.")
    @Test
    void find_all_reservations() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        LocalDate earlierDate = LocalDate.of(2026, 5, 6);

        testHelper.insertWaiting(
                "스타크",
                earlierDate,
                themeId,
                nineTimeId
        );
        testHelper.insertWaiting(
                "피노",
                earlierDate,
                themeId,
                nineTimeId
        );
        testHelper.insertWaiting(
                "피노",
                earlierDate,
                themeId,
                tenTimeId
        );

        ReservationSearchCondition condition = new ReservationSearchCondition("피노");
        List<ReservationResult> reservations = waitingQueryService.findByName(condition);

        ReservationResult first = reservations.getFirst();
        ReservationResult second = reservations.get(1);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(2);
            softly.assertThat(first.id()).isPositive();
            softly.assertThat(first.name()).isEqualTo("피노");
            softly.assertThat(first.date()).isEqualTo(earlierDate);
            softly.assertThat(first.theme()).isEqualTo(ThemeFixture.horrorThemeQueryResult(themeId));
            softly.assertThat(first.time()).isEqualTo(new ReservationTimeResult(nineTimeId, LocalTime.of(9, 0)));
            softly.assertThat(first.status()).isEqualTo(Status.WAITING);
            softly.assertThat(first.rank()).isEqualTo(2L);

            softly.assertThat(second.id()).isPositive();
            softly.assertThat(second.name()).isEqualTo("피노");
            softly.assertThat(second.date()).isEqualTo(earlierDate);
            softly.assertThat(second.theme()).isEqualTo(ThemeFixture.horrorThemeQueryResult(themeId));
            softly.assertThat(second.time()).isEqualTo(new ReservationTimeResult(tenTimeId, LocalTime.of(10, 0)));
            softly.assertThat(second.status()).isEqualTo(Status.WAITING);
            softly.assertThat(second.rank()).isEqualTo(1L);
        });
    }
}
