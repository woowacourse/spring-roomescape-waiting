package roomescape.theme.infrastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.theme.domain.Theme;

@JdbcTest
@Import(ThemeJdbcTemplateRepository.class)
class ThemeJdbcTemplateRepositoryTest {

    private static final String TEST_THEME_NAME = "테스트 테마";
    private static final String TEST_THEME_DESCRIPTION = "테스트 테마 설명";
    private static final String TEST_THEME_THUMBNAIL = "https://good.com/thumb-nail";

    private final JdbcTemplate jdbcTemplate;
    private final ThemeJdbcTemplateRepository themeRepository;

    @Autowired
    public ThemeJdbcTemplateRepositoryTest(ThemeJdbcTemplateRepository themeRepository, JdbcTemplate jdbcTemplate) {
        this.themeRepository = themeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    @DisplayName("테마를 저장한다")
    void save_success() {
        // given
        Theme theme = Theme.create(TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);

        // when
        Theme savedTheme = themeRepository.save(theme);

        // then
        Assertions.assertNotNull(savedTheme.getId());

        Theme expect = theme.appendId(savedTheme.getId());
        Assertions.assertEquals(expect, savedTheme);
    }

    @Test
    @DisplayName("아이디를 기반으로 테마를 찾는다")
    void findById_success() {
        // given
        Theme theme = Theme.create(TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);
        Theme savedTheme = themeRepository.save(theme);

        // when
        Optional<Theme> foundTheme = themeRepository.findById(savedTheme.getId());

        // then
        Assertions.assertEquals(savedTheme.getId(), foundTheme.get().getId());
    }

    @Test
    @DisplayName("존재하지 않는 아이디로 테마를 조회하면 빈 Optional을 반환한다")
    void findById_success_when_not_found() {
        // when
        Long notExistThemeId = 999L;
        Optional<Theme> foundTheme = themeRepository.findById(notExistThemeId);

        // then
        assertTrue(foundTheme.isEmpty());
    }

    @Test
    @DisplayName("모든 테마를 가져온다")
    void findAll_success() {
        // given
        Theme theme1 = Theme.create(TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);
        Theme theme2 = Theme.create(TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);
        themeRepository.save(theme1);
        themeRepository.save(theme2);

        // when
        List<Theme> result = themeRepository.findAll();

        // then
        Assertions.assertEquals(2, result.size());
    }

    @Test
    @DisplayName("테마가 없으면 빈 목록을 반환한다")
    void findAll_success_even_if_no_theme() {
        // when
        List<Theme> result = themeRepository.findAll();

        // then
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("아이디를 기반으로 테마를 삭제한다")
    void deleteById_success() {
        // given
        Theme theme = Theme.create(TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);
        Theme savedTheme = themeRepository.save(theme);

        // when
        themeRepository.deleteById(savedTheme.getId());

        // then
        Optional<Theme> result = themeRepository.findById(savedTheme.getId());
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 테마를 삭제해도 예외가 발생하지 않는다")
    void deleteById_success_even_if_no_theme() {
        // when
        Long notExistThemeId = 999L;
        themeRepository.deleteById(notExistThemeId);

        // then
        Assertions.assertDoesNotThrow(
                () -> themeRepository.deleteById(notExistThemeId)
        );
    }
}
