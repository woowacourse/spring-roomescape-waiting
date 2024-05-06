package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = {"/drop.sql", "/schema.sql", "/data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@JdbcTest
class ThemeRepositoryTest {

    private final ThemeRepository themeRepository;

    @Autowired
    ThemeRepositoryTest(final DataSource dataSource) {
        this.themeRepository = new H2ThemeRepository(dataSource);
    }

    @Test
    @DisplayName("모든 테마 목록을 조회한다.")
    void findAll() {
        // given
        final List<Theme> expected = List.of(
                new Theme(1L, "", "", ""),
                new Theme(2L, "", "", ""),
                new Theme(3L, "", "", ""),
                new Theme(4L, "", "", "")
        );

        // when
        final List<Theme> actual = themeRepository.findAll();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
