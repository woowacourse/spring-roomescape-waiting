package roomescape.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.theme.ThemeSaveRequest;

import static roomescape.TestFixture.THEME_COMIC_DESCRIPTION;
import static roomescape.TestFixture.THEME_COMIC_NAME;
import static roomescape.TestFixture.THEME_COMIC_THUMBNAIL;

class ThemeAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("테마를 성공적으로 생성하면 201을 응답한다.")
    void respondCreatedWhenCreateTheme() {
        final ThemeSaveRequest request
                = new ThemeSaveRequest(THEME_COMIC_NAME, THEME_COMIC_DESCRIPTION, THEME_COMIC_THUMBNAIL);

        assertCreateResponse(request, "/themes", 201);
    }

    @Test
    @DisplayName("테마 목록을 성공적으로 조회하면 200을 응답한다.")
    void respondOkWhenFindThemes() {
        saveTheme();

        assertGetResponse("/themes", 200);
    }

    @Test
    @DisplayName("인기 테마를 성공적으로 조회하면 200을 응답한다.")
    void respondOkWhenFindPopularThemes() {
        saveTheme();

        assertGetResponse("/themes/popular", 200);
    }

    @Test
    @DisplayName("테마를 성공적으로 삭제하면 204를 응답한다.")
    void respondNoContentWhenDeleteThemes() {
        final Long themeId = saveTheme();

        assertDeleteResponse("/themes/", themeId, 204);
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하면 400을 응답한다.")
    void respondBadRequestWhenDeleteNotExistingTheme() {
        saveTheme();
        final Long notExistingThemeId = 0L;

        assertDeleteResponse("/themes/", notExistingThemeId, 400);
    }
}
