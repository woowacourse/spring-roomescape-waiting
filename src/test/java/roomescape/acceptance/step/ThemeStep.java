package roomescape.acceptance.step;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.api.dto.request.ThemeCreateRequest;
import roomescape.controller.api.dto.response.ThemeResponse;
import roomescape.domain.reservation.Theme;
import roomescape.fixture.ThemeFixture;

public class ThemeStep {
    private static ThemeResponse createTheme(final ThemeCreateRequest request) {
        //@formatter:off
        return RestAssured.given().body(request).contentType(ContentType.JSON)
                .when().post("/themes")
                .then().assertThat().statusCode(201).extract().as(ThemeResponse.class);
        //@formatter:on
    }

    public static ThemeResponse 테마_생성(final String themeTitle) {
        final Theme theme = ThemeFixture.getDomain();
        final ThemeCreateRequest request = new ThemeCreateRequest(
                themeTitle,
                theme.getDescription(),
                theme.getThumbnailAsString()
        );
        return createTheme(request);
    }

    public static ThemeResponse 테마_생성() {
        final Theme theme = ThemeFixture.getDomain();
        final ThemeCreateRequest request = new ThemeCreateRequest(
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailAsString()
        );
        return createTheme(request);
    }
}
