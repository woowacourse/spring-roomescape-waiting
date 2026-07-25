package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.ThemePatchRequest;
import roomescape.controller.dto.ThemeRequest;
import roomescape.domain.Theme;
import roomescape.repository.FakeThemeRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeServiceTest {

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        FakeThemeRepository fakeThemeRepository = new FakeThemeRepository();
        themeService = new ThemeService(fakeThemeRepository);
    }

    @Test
    @DisplayName("이름, 설명, 썸네일 URL을 입력받아 테마를 생성한다.")
    void saveTheme() {
        Theme theme = themeService.saveTheme(new ThemeRequest("공포", "귀신의 집", "https://url"));
        assertThat(theme.getName()).isEqualTo("공포");
    }

    @Test
    @DisplayName("모든 테마 목록을 조회하여 반환한다.")
    void allTheme() {
        themeService.saveTheme(new ThemeRequest("공포", "귀신의 집", "https://url"));
        List<Theme> themes = themeService.allTheme();
        assertThat(themes).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 존재하는 특정 테마를 삭제한다.")
    void removeTheme() {
        Theme theme = themeService.saveTheme(new ThemeRequest("공포", "귀신의 집", "https://url"));
        themeService.removeTheme(theme.getId());
        assertThat(themeService.allTheme()).isEmpty();
    }

    @Test
    @DisplayName("기간 및 개수를 지정하여 인기 테마 목록을 조회한다.")
    void findPopularThemes() {
        List<Theme> popularThemes = themeService.findPopularThemes(10L, 7L);
        assertThat(popularThemes).isNotNull();
    }

    @Test
    @DisplayName("식별자를 통해 특정 테마 객체를 조회한다.")
    void findThemeById() {
        Theme saved = themeService.saveTheme(new ThemeRequest("공포", "귀신의 집", "https://url"));
        Theme found = themeService.findThemeById(saved.getId());
        assertThat(found.getName()).isEqualTo("공포");
    }

    @Test
    @DisplayName("테마의 전체 정보를 수정(PUT)하고 반환한다.")
    void putTheme() {
        Theme saved = themeService.saveTheme(new ThemeRequest("공포", "귀신의 집", "https://url"));
        Theme updated = themeService.putTheme(saved.getId(), new ThemeRequest("판타지", "마법의 세계", "https://new"));
        assertThat(updated.getName()).isEqualTo("판타지");
        assertThat(updated.getDescription()).isEqualTo("마법의 세계");
    }

    @Test
    @DisplayName("테마의 일부 정보를 수정(PATCH)하고 반환한다.")
    void patchTheme() {
        Theme saved = themeService.saveTheme(new ThemeRequest("공포", "귀신의 집", "https://url"));
        Theme updated = themeService.patchTheme(saved.getId(), new ThemePatchRequest("미스터리", null, null));
        assertThat(updated.getName()).isEqualTo("미스터리");
        assertThat(updated.getDescription()).isEqualTo("귀신의 집");
    }
}
