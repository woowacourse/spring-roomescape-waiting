package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    private static final int DEFAULT_THEME_COUNT = 5;

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void findAll() {
        Iterable<Theme> result = themeRepository.findAll();

        assertThat(result).hasSize(DEFAULT_THEME_COUNT);
    }

    @DisplayName("id로 테마를 조회한다.")
    @Test
    void findById() {
        Optional<Theme> result = themeRepository.findById(1L);

        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @DisplayName("테마를 생성한다.")
    @Test
    void save() {
        Theme theme = new Theme("테마 이름", "테마 설명", "테마 썸네일");

        themeRepository.save(theme);

        assertThat(themeRepository.findAll()).hasSize(DEFAULT_THEME_COUNT + 1);
    }

    @DisplayName("id로 테마를 삭제한다.")
    @Test
    void deleteById() {
        int result = themeRepository.deleteById(5);

        assertAll(
                () -> assertThat(result).isEqualTo(1),
                () -> assertThat(themeRepository.findAll())
                        .extracting(Theme::getId)
                        .doesNotContain(5L)
        );
    }
}
