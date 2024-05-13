package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import roomescape.domain.Theme;
import roomescape.service.exception.ThemeNotFoundException;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("존재하지 않는 테마 데이터를 조회할 경우 예외가 발생한다.")
    void findByIdNotPresent() {
        long id = 100L;

        assertThatThrownBy(() -> themeRepository.fetchById(id))
                .isInstanceOf(ThemeNotFoundException.class);
    }
}
