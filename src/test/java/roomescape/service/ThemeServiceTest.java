package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.request.ThemeCreateRequest;
import roomescape.exception.DuplicateThemeException;
import roomescape.exception.InvalidInputException;
import roomescape.fake.FakeThemeDao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeServiceTest {

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(new FakeThemeDao());
    }

    @Test
    @DisplayName("테마를 추가할 수 있다.")
    void createTheme() {
        // Given
        ThemeCreateRequest request = new ThemeCreateRequest("테마1", "테마설명", "테마썸네일");
        int originalThemeCount = themeService.findAllThemes().size();

        // When
        themeService.createTheme(request);

        // Then
        assertThat(themeService.findAllThemes()).hasSize(originalThemeCount + 1);
    }

    @Test
    @DisplayName("중복된 이름을 가진 테마는 새로 추가할 수 없다.")
    void cannotCreateTheme() {
        // Given
        ThemeCreateRequest request = new ThemeCreateRequest("중복이름", "테마설명1", "테마썸네일1");
        ThemeCreateRequest request2 = new ThemeCreateRequest("중복이름", "테마설명2", "테마썸네일2");
        themeService.createTheme(request);

        // When & Then
        assertThatThrownBy(() -> themeService.createTheme(request2))
                .isInstanceOf(DuplicateThemeException.class);
    }

    @Test
    @DisplayName("저장되어 있는 테마를 삭제할 수 있다.")
    void deleteThemeById() {
        // Given
        themeService.createTheme(new ThemeCreateRequest("테마", "테마설명", "테마썸네일"));
        int originalThemeCount = themeService.findAllThemes().size();

        // When
        themeService.deleteThemeById(1L);

        // Then
        assertThat(themeService.findAllThemes()).hasSize(originalThemeCount - 1);
    }

    @Test
    @DisplayName("저장되어 있지 않은 테마의 id로는 삭제할 수 없다.")
    void cannotDeleteThemeById() {
        // Given
        // When & Then
        assertThatThrownBy(() -> themeService.deleteThemeById(50L))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("존재하지 않는 테마 id이다.");
    }
}