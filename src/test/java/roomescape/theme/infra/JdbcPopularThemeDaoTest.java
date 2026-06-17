package roomescape.theme.infra;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.support.TestDataHelper;
import roomescape.theme.application.dao.PopularThemeDao;
import roomescape.theme.application.dto.PopularThemeResult;
import roomescape.theme.domain.PopularThemePeriod;

@JdbcTest
@Import(JdbcPopularThemeDao.class)
public class JdbcPopularThemeDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PopularThemeDao themeDao;

    private TestDataHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TestDataHelper(jdbcTemplate);
    }

    @DisplayName("db에서 최근 7일간 인기있던 테마 상위 10개를 조회를 테스트합니다.")
    @Test
    void find_popular_10_themes_during_recent_7days() {
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        LocalDate today = LocalDate.of(2026, 5, 10);
        LocalDate yesterday = today.minusDays(1);

        Long firstThemeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long secondThemeId = testHelper.insertTheme("테마2", "설명2", "img2.jpg");

        testHelper.insertReservation(
                "스타크",
                yesterday,
                firstThemeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "카야",
                yesterday,
                secondThemeId,
                tenTimeId
        );
        testHelper.insertReservation(
                "스타크",
                yesterday,
                firstThemeId,
                tenTimeId
        );

        List<PopularThemeResult> popularThemes = themeDao.findTop10PopularThemes(PopularThemePeriod.from(today));

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(popularThemes.getFirst().id()).isEqualTo(firstThemeId);
            assertSoftly.assertThat(popularThemes.getFirst().reservedCount()).isEqualTo(2);
            assertSoftly.assertThat(popularThemes.get(1).id()).isEqualTo(secondThemeId);
            assertSoftly.assertThat(popularThemes.get(1).reservedCount()).isEqualTo(1);
        });
    }
}
