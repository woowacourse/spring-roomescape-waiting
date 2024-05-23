package roomescape.acceptance.guest;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.response.ThemeResponse;

class ThemeAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("테마 목록을 조회한다.")
    @Test
    void getThemes_success() {
        TypeRef<List<ThemeResponse>> ThemesFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ThemesFormat);
    }

    @DisplayName("예약이 많이 된 테마 목록을 조회한다.")
    @Test
    void getMostReservedThemes() {
        TypeRef<List<ThemeResponse>> ThemesFormat = new TypeRef<>() {
        };

        RestAssured.given().log().all()
                .when().get("/themes/rankings")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ThemesFormat);
    }
}
