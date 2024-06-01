package roomescape.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.support.dto.TokenCookieDto;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class ThemeIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("모든 테마 정보를 조회한다.")
    void readThemes() {
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("data.themes.size()", is(0));
    }

    @Test
    @DisplayName("테마를 추가한다.")
    void createThemes() {
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);

        Map<String, String> params = Map.of(
                "name", "테마명",
                "description", "설명",
                "thumbnail", "http://testsfasdgasd.com"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/themes/1");
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void deleteThemes() {
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);
        Theme theme = themeFixture.createTheme();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .when().delete("/admin/themes/" + theme.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("존재하지 않는 테마ID로 삭제를 요청하면 예외를 발생한다.")
    void failDeleteThemeByNotExistThemeId() {
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(404);
    }

    /*
     *  reservationData DataSet ThemeID 별 reservation 개수
     *  5,4,2,5,2,3,1,1,1,1,1
     *  예약 수 내림차순 + ThemeId 오름차순 정렬 순서
     *  1, 4, 2, 6, 3, 5, 7, 8, 9, 10
     */
    @Test
    @DisplayName("예약 수 상위 10개 테마를 조회했을 때 내림차순으로 정렬된다. 만약 예약 수가 같다면, id 순으로 오름차순 정렬된다.")
    @Sql(scripts = {"/truncate.sql", "/reservationData.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    void readTop10ThemesDescOrder() {
        LocalDate today = LocalDate.now();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes/top?today=" + today)
                .then().log().all()
                .statusCode(200)
                .body("data.themes.size()", is(10))
                .body("data.themes.id", contains(1, 4, 2, 6, 3, 5, 7, 8, 9, 10));
    }

    @ParameterizedTest
    @MethodSource("requestValidateSource")
    @DisplayName("테마 생성 시, 요청 값에 공백 또는 null이 포함되어 있으면 400 에러를 발생한다.")
    void validateBlankRequest(Map<String, String> invalidRequestBody) {
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .body(invalidRequestBody)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(400);
    }

    static Stream<Map<String, String>> requestValidateSource() {
        return Stream.of(
                Map.of(
                        "name", "테마명",
                        "thumbnail", "http://testsfasdgasd.com"
                ),
                Map.of(
                        "name", "",
                        "description", "설명",
                        "thumbnail", "http://testsfasdgasd.com"
                ),
                Map.of(
                        "name", " ",
                        "description", "설명",
                        "thumbnail", "http://testsfasdgasd.com"
                )
        );
    }
}
