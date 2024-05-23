package roomescape.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.core.dto.theme.ThemeRequest;

public class ThemeRequestGenerator {
    private static final String ACCESS_TOKEN;

    static {
        ACCESS_TOKEN = AccessTokenGenerator.generate();
    }

    public static void generateWithName(final String name) {
        final ThemeRequest request = new ThemeRequest(name, "테마 설명", "테마 섬네일");

        RestAssured.given().log().all()
                .cookies("token", ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);
    }
}
