package roomescape.theme.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.theme.fixture.ThemeApiFixture.updateThemeStatus;

import io.restassured.RestAssured;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import roomescape.common.AcceptanceTest;

class ThemeControllerTest extends AcceptanceTest {

    private final String activeThemeName = "활성 테마";
    private final String inactiveThemeName = "비활성 테마";

    @Test
    @DisplayName("활성화된 테마가 없으면 빈 목록을 반환한다.")
    void get_active_themes_empty() {
        Integer themeId = createTheme(managerToken, inactiveThemeName);
        updateThemeStatus(managerToken, themeId, false);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .when().get("/member/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

}
