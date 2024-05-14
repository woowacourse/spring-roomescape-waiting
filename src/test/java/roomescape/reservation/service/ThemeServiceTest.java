package roomescape.reservation.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.dto.SaveThemeRequest;
import roomescape.reservation.model.Theme;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(value = {"/schema.sql", "/data.sql"}, executionPhase = BEFORE_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @DisplayName("전체 테마 정보를 조회한다.")
    @Test
    void getThemesTest() {
        // When
        final List<Theme> themes = themeService.getThemes();

        // Then
        assertThat(themes).hasSize(15);
    }

    @DisplayName("테마 정보를 저장한다.")
    @Test
    void saveThemeTest() {
        // Given
        final String name = "켈리의 두근두근";
        final String description = "켈리와의 두근두근 데이트";
        final String thumbnail = "켈리 사진";
        final SaveThemeRequest saveThemeRequest = new SaveThemeRequest(name, description, thumbnail);

        // When
        final Theme theme = themeService.saveTheme(saveThemeRequest);

        // Then
        final List<Theme> themes = themeService.getThemes();
        Assertions.assertAll(
                () -> assertThat(themes).hasSize(16),
                () -> assertThat(theme.getId()).isEqualTo(16L),
                () -> assertThat(theme.getName().value()).isEqualTo(name),
                () -> assertThat(theme.getDescription().value()).isEqualTo(description),
                () -> assertThat(theme.getThumbnail().value()).isEqualTo(thumbnail)
        );
    }

    @DisplayName("테마 정보를 삭제한다.")
    @Test
    void deleteThemeTest() {
        // When
        themeService.deleteTheme(7L);

        // Then
        final List<Theme> themes = themeService.getThemes();
        assertThat(themes).hasSize(14);
    }

    @DisplayName("해당 테마 정보를 참조하고 있는 예약이 하나라도 있으면 삭제시 예외가 발생한다.")
    @Test
    void throwExceptionWhenDeleteThemeHasRelation() {
        // Given
        final long themeId = 1;
        // When & Then
        assertThatThrownBy(() -> themeService.deleteTheme(themeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약에 포함된 테마 정보는 삭제할 수 없습니다.");
    }
}
