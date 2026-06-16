package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.theme.adapter.out.persistence.JpaThemeRepository;
import roomescape.theme.domain.Theme;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaThemeRepository.class)
class JpaThemeRepositoryTest {
    @Autowired
    private JpaThemeRepository repository;

    @Test
    @DisplayName("테마를 저장할 수 있다.")
    void saves_theme_successfully() {
        Theme theme = new Theme(null, "무서운게 딱 좋아", "무서운 분위기의 방탈출", "https://example.com/theme.jpg");

        Theme savedTheme = repository.save(theme);

        assertThat(savedTheme.getName()).isEqualTo("무서운게 딱 좋아");
        assertThat(savedTheme.getDescription()).isEqualTo("무서운 분위기의 방탈출");
        assertThat(savedTheme.getThumbnailUrl()).isEqualTo("https://example.com/theme.jpg");
    }

    @Test
    @DisplayName("테마를 삭제할 수 있다.")
    void deletes_theme_successfully() {
        // given
        Theme theme = new Theme(null, "무서운게 딱 좋아", "무서운 분위기의 방탈출", "https://example.com/theme.jpg");
        Theme savedTheme = repository.save(theme);

        // when
        repository.deleteById(savedTheme.getId());

        // then
        List<Theme> themes = repository.findAll();
        assertThat(themes).hasSize(4);
        assertThat(themes).extracting(Theme::getId)
                .doesNotContain(savedTheme.getId());
    }

    @Test
    @DisplayName("각 날짜에 존재하는 모든 테마를 조회할 수 있다.")
    void finds_themes_available_on_each_date() {
        List<Theme> themes = repository.findThemesBySlotDate(LocalDate.of(2026, 5, 5));

        assertThat(themes).hasSize(4);
        assertThat(themes).extracting(Theme::getId)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("최근 7일 예약 개수에 따른 인기 테마를 조회할 수 있다.")
    void finds_popular_themes_by_reservations_in_last_seven_days() {
        // given
        LocalDate currentDate = LocalDate.of(2026, 5, 10);

        // when
        List<Theme> themes = repository.findPopularThemeByCurrentDate(currentDate);

        assertThat(themes).hasSize(3);
        assertThat(themes).extracting(Theme::getId)
                .containsExactly(2L, 1L, 3L);
    }

    @Test
    @DisplayName("전체 테마를 조회할 수 있다.")
    void finds_all_themes_successfully() {
        List<Theme> themes = repository.findAll();

        assertThat(themes).hasSize(4);
        assertThat(themes)
                .extracting(Theme::getId)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("이미 존재하는 테마이면 true를 반환한다.")
    void existing_theme_returns_true() {
        // given
        String themeName = "세기의 도둑";

        // when
        boolean result = repository.existsAlreadyTheme(themeName);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 테마이면 false를 반환한다.")
    void missing_theme_returns_false() {
        // given
        String themeName = "이삭";

        // when
        boolean result = repository.existsAlreadyTheme(themeName);

        // then
        assertThat(result).isFalse();
    }
}
