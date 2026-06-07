package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.theme.fixture.PopularThemeResultFixture.popularThemeResult;
import static roomescape.theme.fixture.ThemeFixture.theme;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.projection.PopularThemeResult;

@JdbcTest
class ThemeRepositoryTest {

    private JdbcThemeRepository jdbcThemeRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);
    }

    private List<Theme> saveAll(List<Theme> themes) {
        List<Theme> savedThemes = new ArrayList<>();
        for (Theme theme : themes) {
            Theme savedTheme = jdbcThemeRepository.save(theme);
            savedThemes.add(savedTheme);
        }
        return savedThemes;
    }


    @Nested
    @DisplayName("findAll 메서드는")
    class FindAllTest {


        @Test
        @DisplayName("모든 테마를 조회한다")
        void 성공() {
            // given
            List<Theme> themes = List.of(
                theme("테마1"),
                theme("테마2"),
                theme("테마3")
            );
            saveAll(themes);

            // when
            List<Theme> actual = jdbcThemeRepository.findAll();

            // then
            assertThat(actual)
                .hasSize(themes.size());
        }
    }

    @Nested
    @DisplayName("findById 메서드는")
    class FindByIdTest {


        @Test
        @DisplayName("요청한 Id를 가진 테마를 조회한다")
        void 성공() {
            // given
            Theme savedTheme = jdbcThemeRepository.save(theme());

            // when
            Theme actual = jdbcThemeRepository.findById(savedTheme.getId()).get();

            // then
            assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(savedTheme);
        }


        @Test
        @DisplayName("잘못된 id이면 optional.empty를 반환한다")
        void 실패() {
            // given
            Long wrongId = Long.MIN_VALUE;

            // when
            Optional<Theme> actual = jdbcThemeRepository.findById(wrongId);

            // then
            assertThat(actual)
                .isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIsActive 메서드는")
    class FindByIsActiveTest {


        @Test
        @DisplayName("status가 isActive인 테마만 조회한다")
        void 성공() {
            // given
            List<Theme> themes = saveAll(List.of(
                ThemeFixture.activeTheme("다테마"),
                ThemeFixture.activeTheme("나테마"),
                ThemeFixture.activeTheme("가테마"))
            );
            Collections.sort(themes, Comparator.comparing(Theme::getName));

            // when
            List<Theme> actual = jdbcThemeRepository.findByIsActive(true);

            // then
            assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(themes);
        }
    }

    @Nested
    @DisplayName("save 메서드는")
    class SaveTest {


        @Test
        @DisplayName("테마를 생성한다")
        void 성공() {
            // given
            List<Theme> themes = List.of();

            // when
            jdbcThemeRepository.save(theme());

            // then
            assertThat(jdbcThemeRepository.findAll())
                .hasSize(themes.size() + 1);
        }
    }

    @Nested
    @DisplayName("updateStatus 메서드는")
    class UpdateStatusTest {


        @Test
        @DisplayName("테마 상태를 변경한다 - isActive: true")
        void 성공1() {
            // given
            Theme savedTheme = jdbcThemeRepository.save(theme());
            savedTheme.updateStatus(true);

            // when
            jdbcThemeRepository.updateStatus(savedTheme);

            // then
            assertThat(jdbcThemeRepository.findById(savedTheme.getId()).get().isActive())
                .isTrue();
        }


        @Test
        @DisplayName("테마 상태를 변경한다 - isActive: false")
        void 성공2() {
            // given
            Theme savedTheme = jdbcThemeRepository.save(ThemeFixture.activeTheme());
            savedTheme.updateStatus(false);

            // when
            jdbcThemeRepository.updateStatus(savedTheme);

            // then
            assertThat(jdbcThemeRepository.findById(savedTheme.getId()).get().isActive())
                .isFalse();
        }
    }

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
            List<PopularThemeResult> actual = jdbcThemeRepository.findPopularThemes(startDate,
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
            List<PopularThemeResult> actual = jdbcThemeRepository.findPopularThemes(startDate,
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
            List<PopularThemeResult> actual = jdbcThemeRepository.findPopularThemes(startDate, endDate, limit);

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
            List<PopularThemeResult> actual = jdbcThemeRepository.findPopularThemes(startDate, endDate, limit);

            // then
            assertThat(actual)
                .extracting(PopularThemeResult::reservationCount)
                .containsExactly(expected.toArray(new Long[0]));
        }
    }
}
