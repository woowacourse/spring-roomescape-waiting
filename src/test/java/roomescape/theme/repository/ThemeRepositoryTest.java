package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.theme.fixture.PopularThemeResultFixture.popularThemeResult;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.repository.projection.PopularThemeResult;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Nested
    @DisplayName("findPopularThemes 메서드는")
    @Sql(
        scripts = {"classpath:truncate.sql", "classpath:test-popular-theme.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    class FindPopularThemesTest {

        static final int POPULAR_STATISTICS_DURATION = 7;
        static final int PREVIOUS_DAYS = 1;

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(PREVIOUS_DAYS);
        LocalDate startDate = today.minusDays(PREVIOUS_DAYS + POPULAR_STATISTICS_DURATION);
        int limit = 10;


        @Test
        @DisplayName("입력된 기간 사이에 등록된 예약의 상위 n개의 테마를 조회한다")
        void 성공1() {
            // given
            List<PopularThemeResult> expected = List.of(
                popularThemeResult(1L, "인기 테마 1", 4),
                popularThemeResult(2L, "인기 테마 2", 2),
                popularThemeResult(3L, "인기 테마 3", 1)
            );

            // when
            List<PopularThemeResult> actual = themeRepository.findPopularThemes(startDate,
                endDate, limit);

            // then
            assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("description", "thumbnailUrl")
                .isEqualTo(expected);
        }


        @Test
        @DisplayName("비활성화된 테마는 조회 결과에서 제외된다")
        void 성공2() {
            // given
            int expected = 3;

            // when
            List<PopularThemeResult> actual = themeRepository.findPopularThemes(startDate,
                endDate, limit);

            // then
            assertThat(actual)
                .hasSize(expected);
        }


        @Test
        @DisplayName("요청 개수만큼 조회한다")
        void 성공3() {
            // given
            int limit = 2;
            List<PopularThemeResult> expected = List.of(
                popularThemeResult(1L, "인기 테마 1", 4),
                popularThemeResult(2L, "인기 테마 2", 2)
            );

            // when
            List<PopularThemeResult> actual = themeRepository.findPopularThemes(startDate, endDate, limit);

            // then
            assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("description", "thumbnailUrl")
                .isEqualTo(expected);
        }


        @Test
        @DisplayName("예약 완료 상태의 예약만 인기 테마 집계에 포함한다")
        void 성공4() {
            // given
            List<Long> expected = List.of(4L, 2L, 1L);

            // when
            List<PopularThemeResult> actual = themeRepository.findPopularThemes(startDate, endDate, limit);

            // then
            assertThat(actual)
                .extracting(PopularThemeResult::reservationCount)
                .containsExactly(expected.toArray(new Long[0]));
        }
    }
}
