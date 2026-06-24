package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.theme.Theme;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeThemeRepository;

class ThemeServiceTest {

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        FakeThemeRepository fakeThemeRepository = new FakeThemeRepository();
        FakeReservationRepository fakeReservationRepository = new FakeReservationRepository();
        themeService = new ThemeService(fakeThemeRepository, fakeReservationRepository);
    }

    @Test
    @DisplayName("이름, 설명, 썸네일 URL, 가격을 입력받아 테마를 생성한다.")
    void 테마_저장() {
        Theme theme = themeService.saveTheme("공포", "귀신의 집", "https://url", 50000L);
        assertThat(theme.getName()).isEqualTo("공포");
        assertThat(theme.getPrice()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("모든 테마 목록을 조회하여 반환한다.")
    void 전체_테마_조회() {
        themeService.saveTheme("공포", "귀신의 집", "https://url", 50000L);
        List<Theme> themes = themeService.findAllThemes();
        assertThat(themes).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 존재하는 특정 테마를 삭제한다.")
    void 테마_삭제() {
        Theme theme = themeService.saveTheme("공포", "귀신의 집", "https://url", 50000L);
        themeService.removeTheme(theme.getId());
        assertThat(themeService.findAllThemes()).isEmpty();
    }

    @Test
    @DisplayName("기간 및 개수를 지정하여 인기 테마 목록을 조회한다.")
    void 인기_테마_조회() {
        List<Theme> popularThemes = themeService.findPopularThemes(10L, 7L);
        assertThat(popularThemes).isNotNull();
    }
}
