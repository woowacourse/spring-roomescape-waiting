package roomescape.acceptance;

import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.theme.ThemeResponse;
import roomescape.dto.theme.ThemeSaveRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.*;

class ThemeAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("테마를 성공적으로 생성하면 201을 응답한다.")
    void respondCreatedWhenCreateTheme() {
        final ThemeSaveRequest request
                = new ThemeSaveRequest(THEME_HORROR_NAME, THEME_HORROR_DESCRIPTION, THEME_HORROR_THUMBNAIL);

        final ThemeResponse response = assertPostResponse(request, "/themes", 201)
                .extract().as(ThemeResponse.class);

        assertAll(() -> {
            assertThat(response.name()).isEqualTo(THEME_HORROR_NAME);
            assertThat(response.description()).isEqualTo(THEME_HORROR_DESCRIPTION);
            assertThat(response.thumbnail()).isEqualTo(THEME_HORROR_THUMBNAIL);
        });
    }

    @Test
    @DisplayName("테마 목록을 성공적으로 조회하면 200을 응답한다.")
    void respondOkWhenFindThemes() {
        saveTheme();

        final JsonPath jsonPath = assertGetResponse("/themes", 200)
                .extract().response().jsonPath();

        assertAll(() -> {
            assertThat(jsonPath.getString("name[0]")).isEqualTo(THEME_HORROR_NAME);
            assertThat(jsonPath.getString("description[0]")).isEqualTo(THEME_HORROR_DESCRIPTION);
            assertThat(jsonPath.getString("thumbnail[0]")).isEqualTo(THEME_HORROR_THUMBNAIL);
        });
    }

    @Test
    @DisplayName("인기 테마를 성공적으로 조회하면 200을 응답한다.")
    void respondOkWhenFindPopularThemes() {
        saveTheme();

        final JsonPath jsonPath = assertGetResponse("/themes/popular", 200)
                .extract().response().jsonPath();

        assertAll(() -> {
            assertThat(jsonPath.getString("name[0]")).isEqualTo(THEME_HORROR_NAME);
            assertThat(jsonPath.getString("description[0]")).isEqualTo(THEME_HORROR_DESCRIPTION);
            assertThat(jsonPath.getString("thumbnail[0]")).isEqualTo(THEME_HORROR_THUMBNAIL);
        });
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
