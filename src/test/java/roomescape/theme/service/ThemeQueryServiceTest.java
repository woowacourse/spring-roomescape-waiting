package roomescape.theme.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.ThemeFixture;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;
import roomescape.theme.application.dto.PopularThemeResult;
import roomescape.theme.application.dto.ThemeResult;
import roomescape.theme.application.service.ThemeQueryService;

@ServiceTest
public class ThemeQueryServiceTest {

    private static final LocalDate CURRENT_DATE = LocalDate.of(2026, 5, 6);

    @Autowired
    private ThemeQueryService themeQueryService;

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("테마의 전체 조회를 테스트합니다.")
    @Test
    void find_all_themes() {
        Long firstId = testHelper.insertTheme(ThemeFixture.themeCreateCommand(1));
        Long secondId = testHelper.insertTheme(ThemeFixture.themeCreateCommand(2));
        Long thirdId = testHelper.insertTheme(ThemeFixture.themeCreateCommand(3));

        List<ThemeResult> themes = themeQueryService.findAll();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themes.size()).isEqualTo(3);
            softly.assertThat(themes).containsExactly(
                    ThemeFixture.themeQueryResult(1, firstId),
                    ThemeFixture.themeQueryResult(2, secondId),
                    ThemeFixture.themeQueryResult(3, thirdId)
            );
        });
    }

    @DisplayName("인기 테마 조회를 테스트합니다.")
    @Test
    void find_popular_themes() {
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        Long firstThemeId = testHelper.insertTheme(ThemeFixture.themeCreateCommand(1));
        Long secondThemeId = testHelper.insertTheme(ThemeFixture.themeCreateCommand(2));

        testHelper.insertReservation(
                "테마1 예약자1",
                CURRENT_DATE.minusDays(1),
                firstThemeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "테마1 예약자2",
                CURRENT_DATE.minusDays(1),
                firstThemeId,
                tenTimeId
        );
        testHelper.insertReservation(
                "테마2 예약자1",
                CURRENT_DATE.minusDays(2),
                secondThemeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "기간 밖 예약자",
                CURRENT_DATE.minusDays(8),
                secondThemeId,
                tenTimeId
        );

        List<PopularThemeResult> responses = themeQueryService.findPopularThemes(CURRENT_DATE);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).containsExactly(
                    new PopularThemeResult(firstThemeId, "테마 1", "테마 설명 1", "http://img.url", 2),
                    new PopularThemeResult(secondThemeId, "테마 2", "테마 설명 2", "http://img.url", 1)
            );
        });
    }
}
