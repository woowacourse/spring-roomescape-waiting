package roomescape.domain.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;
import static roomescape.fixture.ThemeFixture.DUMMY_THEME;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.dto.ThemeAddRequest;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.global.exception.NoMatchingDataException;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @InjectMocks
    ThemeService themeService;

    @Mock
    ThemeRepository themeRepository;

    @DisplayName("모든 테마를 불러올 수 있습니다.")
    @Test
    void should_get_all_theme() {
        ThemeRepository themeRepository = new FakeThemeRepository();
        themeService = new ThemeService(themeRepository);
        themeRepository.save(new Theme(1L, "테마1", "테마1설명", "url"));

        List<Theme> allTheme = themeService.findAllTheme();

        assertThat(allTheme.size()).isOne();
    }

    @DisplayName("테마를 추가할 수 있습니다.")
    @Test
    void should_add_theme() {
        ThemeRepository themeRepository = new FakeThemeRepository();
        themeService = new ThemeService(themeRepository);
        Theme expectedTheme = new Theme(1L, "테마1", "테마1설명", "url");

        Theme savedTheme = themeService.addTheme(new ThemeAddRequest("테마1", "테마1설명", "url"));

        assertThat(savedTheme).isEqualTo(expectedTheme);
    }

    @DisplayName("존재하지 않는 테마 삭제 요청시 예외가 발생합니다")
    @Test
    void should_throw_ClientIllegalArgumentException_when_theme_id_no_exist() {
        ThemeRepository themeRepository = new FakeThemeRepository();
        themeService = new ThemeService(themeRepository);
        assertThatThrownBy(() -> themeService.removeTheme(1L))
                .isInstanceOf(NoMatchingDataException.class)
                .hasMessage("해당 id를 가진 테마가 존재하지 않습니다.");
    }

    @DisplayName("테마를 삭제할 수 있습니다")
    @Test
    void should_remove_theme() {
        FakeThemeRepository fakeThemeRepository = new FakeThemeRepository();
        fakeThemeRepository.save(DUMMY_THEME);
        themeService = new ThemeService(fakeThemeRepository);

        themeService.removeTheme(1L);

        assertThat(fakeThemeRepository.themes).hasSize(0);
    }

    @DisplayName("인기 테마를 알 수 있습니다.")
    @Test
    void should_get_theme_ranking() {
        Theme theme = new Theme(1L, "테마1", "테마1설명", "url");
        when(themeRepository.findThemeOrderByReservationCount()).thenReturn(List.of(theme));

        List<Theme> themeRanking = themeService.getThemeRanking();

        assertAll(
                () -> assertThat(themeRanking).hasSize(1),
                () -> assertThat(themeRanking.get(0)).isEqualTo(theme)
        );
    }
}
