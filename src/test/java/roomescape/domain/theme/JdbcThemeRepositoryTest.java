package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        themeRepository = new JdbcThemeRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("테마를 저장한다.")
    void save() {
        Theme theme = Theme.createWithoutId("테마", "설명", "url");
        Theme saved = themeRepository.save(theme);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테마");
    }

    @Test
    @DisplayName("모든 테마를 조회한다.")
    void findAll() {
        int beforeSize = themeRepository.findAll().size();
        themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));

        List<Theme> themes = themeRepository.findAll();

        assertThat(themes).hasSize(beforeSize + 1);
    }

    @Test
    @DisplayName("ID로 테마를 삭제한다.")
    void deleteById() {
        Theme saved = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));
        int beforeSize = themeRepository.findAll().size();

        int deletedCount = themeRepository.deleteById(saved.getId());

        assertThat(deletedCount).isEqualTo(1);
        assertThat(themeRepository.findAll()).hasSize(beforeSize - 1);
    }
}
