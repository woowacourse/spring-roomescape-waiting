package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.repository.fake.FakeThemeRepository;
import roomescape.entity.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.exception.custom.DuplicatedException;

class ThemeServiceTest {

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(new FakeThemeRepository());
    }

    @Test
    @DisplayName("모든 테마를 가져올 수 있다.")
    void findAllThemes() {
        ThemeRequest request1 = new ThemeRequest("테마1", "설명1", "썸네일1");
        ThemeRequest request2 = new ThemeRequest("테마2", "설명2", "썸네일2");

        themeService.addTheme(request1);
        themeService.addTheme(request2);

        assertThat(themeService.findAllThemes()).hasSize(2);
    }

    @Test
    @DisplayName("테마를 추가를 할 수 있다.")
    void addTheme() {
        ThemeRequest request = new ThemeRequest("테마", "설명", "썸네일");

        Theme actual = themeService.addTheme(request);

        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getName()).isEqualTo("테마");
            assertThat(actual.getDescription()).isEqualTo("설명");
            assertThat(actual.getThumbnail()).isEqualTo("썸네일");
        });
    }

    @Test
    @DisplayName("테마가 이미 존재한다면 추가할 수 없다.")
    void addDuplicatedTheme() {
        ThemeRequest request = new ThemeRequest("테마", "설명", "썸네일");

        themeService.addTheme(request);

        assertThatThrownBy(() -> themeService.addTheme(request))
            .isInstanceOf(DuplicatedException.class)
            .hasMessageContaining("theme");
    }

    @Test
    @DisplayName("테마를 id를 통해 제거할 수 있다.")
    void removeTheme() {
        ThemeRequest request = new ThemeRequest("테마", "설명", "썸네일");
        themeService.addTheme(request);

        themeService.removeTheme(1L);

        assertThat(themeService.findAllThemes()).hasSize(0);
    }
}
