package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Theme;
import roomescape.service.exception.ThemeNotFoundException;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    ThemeRepository themeRepository;

    @Test
    @DisplayName("모든 테마 목록을 조회한다.")
    void findAll() {
        final List<Theme> expected = List.of(
                new Theme(1L, null, null, null),
                new Theme(2L, null, null, null),
                new Theme(3L, null, null, null),
                new Theme(4L, null, null, null)
        );

        assertThat(themeRepository.findAll()).isEqualTo(expected);
    }

    @Test
    @DisplayName("존재하지 않는 테마 데이터를 조회할 경우 예외가 발생한다.")
    void findByIdNotPresent() {
        long id = 100L;

        assertThatThrownBy(() -> themeRepository.fetchById(id)).isInstanceOf(
                ThemeNotFoundException.class);
    }
}
