package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.config.TestConfig;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;

@SpringBootTest(classes = TestConfig.class)
@Transactional
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("모든 테마들을 조회한다.")
    void getAllThemes() {
        Theme theme = new Theme("테마", "테마 설명", "https://example.com");
        Theme savedTheme = themeRepository.save(theme);

        List<ThemeResponse> themeResponses = themeService.getAllThemes();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeResponses).hasSize(1);
            softly.assertThat(themeResponses.get(0).id()).isEqualTo(savedTheme.getId());
            softly.assertThat(themeResponses.get(0).name()).isEqualTo("테마");
            softly.assertThat(themeResponses.get(0).description()).isEqualTo("테마 설명");
            softly.assertThat(themeResponses.get(0).thumbnail()).isEqualTo("https://example.com");
        });
    }

    @Test
    @DisplayName("테마를 추가한다.")
    void addTheme() {
        ThemeRequest request = new ThemeRequest("테마", "테마 설명", "https://example.com");

        ThemeResponse themeResponse = themeService.addTheme(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeResponse.name()).isEqualTo("테마");
            softly.assertThat(themeResponse.description()).isEqualTo("테마 설명");
            softly.assertThat(themeResponse.thumbnail()).isEqualTo("https://example.com");
        });
    }

    @Test
    @DisplayName("id로 테마를 조회한다.")
    void deleteThemeById() {
        Theme theme = new Theme("테마", "테마 설명", "https://example.com");
        Theme savedTheme = themeRepository.save(theme);

        themeService.deleteThemeById(savedTheme.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeRepository.findById(savedTheme.getId())).isEmpty();
        });
    }

    @Test
    @DisplayName("인기있는 테마들을 조회한다.")
    @Sql("/popular-themes.sql")
    void getPopularThemes() {
        LocalDate stateDate = LocalDate.of(2024, 4, 6);
        LocalDate endDate = LocalDate.of(2024, 4, 10);
        int limit = 3;

        List<ThemeResponse> popularThemes = themeService.getPopularThemes(stateDate, endDate, limit);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(3);

            softly.assertThat(popularThemes.get(0).id()).isEqualTo(4);
            softly.assertThat(popularThemes.get(0).name()).isEqualTo("마법의 숲");
            softly.assertThat(popularThemes.get(0).description()).isEqualTo("요정과 마법사들이 사는 신비로운 숲 속으로!");
            softly.assertThat(popularThemes.get(0).thumbnail()).isEqualTo("https://via.placeholder.com/150/30f9e7");

            softly.assertThat(popularThemes.get(1).id()).isEqualTo(3);
            softly.assertThat(popularThemes.get(1).name()).isEqualTo("시간여행");
            softly.assertThat(popularThemes.get(1).description()).isEqualTo("과거와 미래를 오가며 역사의 비밀을 밝혀보세요.");
            softly.assertThat(popularThemes.get(1).thumbnail()).isEqualTo("https://via.placeholder.com/150/24f355");

            softly.assertThat(popularThemes.get(2).id()).isEqualTo(2);
            softly.assertThat(popularThemes.get(2).name()).isEqualTo("우주 탐험");
            softly.assertThat(popularThemes.get(2).description()).isEqualTo("끝없는 우주에 숨겨진 비밀을 파헤치세요.");
            softly.assertThat(popularThemes.get(2).thumbnail()).isEqualTo("https://via.placeholder.com/150/771796");
        });
    }
}
