package roomescape.theme.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.theme.adapter.out.persistence.JdbcThemeRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import(JdbcThemeRepository.class)
class JdbcThemeRepositoryTest {
    @Autowired
    private JdbcThemeRepository repository;

    @Test
    void 테마_저장_레포지토리_테스트() {
        Theme theme = new Theme(null, "무서운게 딱 좋아", "무서운 분위기의 방탈출", "https://example.com/theme.jpg");

        Theme savedTheme = repository.save(theme);

        assertThat(savedTheme.getName()).isEqualTo("무서운게 딱 좋아");
        assertThat(savedTheme.getDescription()).isEqualTo("무서운 분위기의 방탈출");
        assertThat(savedTheme.getThumbnailUrl()).isEqualTo("https://example.com/theme.jpg");
    }

    @Test
    void 테마_삭제_레포지토리_테스트() {
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
    void 각_날짜에_존재하는_모든_테마_조회_레포토지리_테스트() {
        List<Theme> themes = repository.findThemesBySlotDate(LocalDate.of(2026, 5, 5));

        assertThat(themes).hasSize(4);
        assertThat(themes).extracting(Theme::getId)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    }

    @Test
    void 최근_7일_예약_개수에_따른_인기_테마_조회_레포지토리_테스트() {
        // given
        LocalDate currentDate = LocalDate.of(2026, 5, 10);

        // when
        List<Theme> themes = repository.findPopularThemeByCurrentDate(currentDate);

        assertThat(themes).hasSize(3);
        assertThat(themes).extracting(Theme::getId)
                .containsExactly(2L, 1L, 3L);
    }

    @Test
    void 테마_전체_조회_레포지토리_테스트() {
        List<Theme> themes = repository.findAll();

        assertThat(themes).hasSize(4);
        assertThat(themes)
                .extracting(Theme::getId)
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("이미 존재하는 테마이면 true를 반환한다.")
    void existsAlreadyTheme_테스트_1() {
        // given
        String themeName = "세기의 도둑";

        // when
        boolean result = repository.existsAlreadyTheme(themeName);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 테마이면 false를 반환한다.")
    void existsAlreadyTheme_테스트_2() {
        // given
        String themeName = "이삭";

        // when
        boolean result = repository.existsAlreadyTheme(themeName);

        // then
        assertThat(result).isFalse();
    }
}
