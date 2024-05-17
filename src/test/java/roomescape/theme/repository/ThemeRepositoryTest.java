package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.test.RepositoryTest;
import roomescape.theme.domain.Theme;

class ThemeRepositoryTest extends RepositoryTest {
    private static final int COUNT_OF_THEME = 3;
    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("전체 테마를 조회할 수 있다.")
    @Test
    void findAllTest() {
        List<Theme> actual = themeRepository.findAll();

        assertThat(actual).hasSize(COUNT_OF_THEME);
    }

    @DisplayName("id를 통해 테마를 조회할 수 있다.")
    @Test
    void findByIdTest() {
        Optional<Theme> actual = themeRepository.findById(1L);

        assertThat(actual.get().getId()).isEqualTo(1L);
    }

    @DisplayName("인기 테마를 조회할 수 있다.")
    @Test
    void findPopularThemeTest() {
        List<Theme> expected = List.of(
                new Theme(1L, "레벨2 탈출", "우테코 레벨2 탈출기!", "https://img.jpg"),
                new Theme(2L, "레벨3 탈출", "우테코 레벨3 탈출기!", "https://img.jpg")
        );
        LocalDate startDate = LocalDate.now().minusDays(3);
        LocalDate endDate = LocalDate.now();
        int count = 2;

        List<Theme> actual = themeRepository.findOrderByReservationCount(startDate, endDate, count);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("테마를 저장할 수 있다.")
    @Test
    void saveTest() {
        Theme newTheme = new Theme("우테코 탈출", "우테코 탈출기!", "https://img.jpg");

        themeRepository.save(newTheme);

        Optional<Theme> savedTheme = themeRepository.findById(COUNT_OF_THEME + 1L);
        assertThat(savedTheme).isNotEmpty();
    }

    @DisplayName("테마를 삭제할 수 있다.")
    @Test
    void deleteByIdTest() {
        themeRepository.deleteById(3L);

        Optional<Theme> savedTheme = themeRepository.findById(3L);
        assertThat(savedTheme).isEmpty();
    }
}
