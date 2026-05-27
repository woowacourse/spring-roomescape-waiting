package roomescape.theme.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.theme.fixture.ThemeApiFixture.updateThemeStatus;

import io.restassured.RestAssured;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.common.AcceptanceTest;

class ThemeControllerTest extends AcceptanceTest {

    private final String activeThemeName = "활성 테마";
    private final String inactiveThemeName = "비활성 테마";


    @Nested
    @DisplayName("getActiveThemes 메서드는")
    class GetTest {


        @Test
        @DisplayName("모든 활성화된 테마를 조회한다")
        void 성공() {
            Integer activeThemeId = createTheme(managerToken, activeThemeName);
            Integer inactiveThemeId = createTheme(managerToken, inactiveThemeName);

            updateThemeStatus(managerToken, activeThemeId, true);
            updateThemeStatus(managerToken, inactiveThemeId, false);

            RestAssured.given().log().all()
                .when().get("/member/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
        }
    }
}
