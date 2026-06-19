package roomescape.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeStatus;
import roomescape.domain.fixture.ReservationFixture;
import roomescape.repository.ReservationRepository;
import roomescape.service.result.ThemeTimesResult;
import roomescape.support.IntegrationTest;
import roomescape.support.TestDateTimes;

@IntegrationTest
@Sql("/integration-fixture.sql")
class ThemeTimesQueryIntegrationTest {

    private final Theme theme = Theme.restore(1L, "공포", "어마무시한 공포 테마", "https://theme.com/image.png", 30000L, true);
    private final ReservationTime time = ReservationTime.restore(1L, TestDateTimes.defaultTime(), TimeStatus.ACTIVE);
    @Autowired
    private ThemeQueryRepository themeQueryRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 미래_날짜의_테마별_시간_상태를_예약_가능과_대기_가능으로_조회한다() {
        // given
        LocalDate date = TestDateTimes.tomorrow();
        reservationRepository.save(ReservationFixture.createWithAll("이프", date, theme, time));

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
        LocalDate date = TestDateTimes.yesterday();

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
