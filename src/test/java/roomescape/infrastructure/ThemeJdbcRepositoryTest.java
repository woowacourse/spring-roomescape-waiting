package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;

@Sql(scripts = {"/test-schema.sql"})
@JdbcTest
class ThemeJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ThemeRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ThemeJdbcRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("테마를 아이디로 조회한다.")
    void findById() {
        // given
        var theme = readyTheme();
        var savedId = repository.save(theme);

        // when
        Optional<Theme> found = repository.findById(savedId);

        // then
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("테마를 저장한다.")
    void save() {
        // given
        var theme = readyTheme();

        // when
        repository.save(theme);

        // then
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void removeById() {
        // given
        var theme = readyTheme();
        var savedId = repository.save(theme);

        // when
        repository.removeById(savedId);

        // then
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("모든 테마를 조회한다.")
    void findAll() {
        // given
        var theme1 = new Theme(1L, "테마1", "설명", "썸네일");
        var theme2 = new Theme(2L, "테마2", "설명", "썸네일");
        repository.save(theme1);
        repository.save(theme2);

        // when
        var themes = repository.findAll();

        // then
        assertThat(themes).hasSize(2);
    }

    private Theme readyTheme() {
        return new Theme(1L, "테마", "설명", "썸네일");
    }
}
