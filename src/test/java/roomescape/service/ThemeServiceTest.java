package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Theme;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeThemeRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ThemeServiceTest {
    
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        FakeThemeRepository fakeThemeRepository = new FakeThemeRepository();
        FakeReservationRepository fakeReservationRepository = new FakeReservationRepository();
        themeService = new ThemeService(fakeThemeRepository, fakeReservationRepository);
    }

    @Test
    @DisplayName("테마 정보를 입력하여 새로운 테마를 생성하고 반환한다.")
    void saveTime() {
        Theme theme = themeService.saveTheme("공포", "무섭습니다", "xxx.example.com", 15000L);
        assertThat(theme.getName()).isEqualTo("공포");
        assertThat(theme.getDescription()).isEqualTo("무섭습니다");
        assertThat(theme.getThumbnailUrl()).isEqualTo("xxx.example.com");
    }

    @Test
    @DisplayName("존재하는 테마 정보를 삭제하면 전체 목록에서 사라진다.")
    void removeTime() {
        Theme theme = themeService.saveTheme("공포", "무섭습니다", "xxx.example.com", 15000L);
        themeService.removeTheme(theme.getId());
        assertThat(themeService.allTheme()).isEmpty();
    }

    @Test
    @DisplayName("모든 테마 목록을 조회하여 반환한다.")
    void allTimes() {
        Theme theme = themeService.saveTheme("공포", "무섭습니다", "xxx.example.com", 15000L);
        List<Theme> themes = themeService.allTheme();
        assertThat(themes).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 테마를 조회한다.")
    void findTime() {
        Theme theme = themeService.saveTheme("공포", "무섭습니다", "xxx.example.com", 15000L);
        Theme foundTime = themeService.findTheme(theme.getId());
        assertThat(foundTime.getName()).isEqualTo("공포");
        assertThat(foundTime.getDescription()).isEqualTo("무섭습니다");
        assertThat(foundTime.getThumbnailUrl()).isEqualTo("xxx.example.com");
    }
}
