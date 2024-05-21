package roomescape.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.dto.request.ThemeRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "classpath:test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("theme 목록 조회 요청이 올바르게 동작한다.")
    @Test
    void given_when_GetThemes_then_statusCodeIsOkay() {
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(15));
    }

    @DisplayName("theme 등록 및 삭제 요청이 올바르게 동작한다.")
    @Test
    void given_themeRequest_when_postAndDeleteTheme_then_statusCodeIsOkay() {
        ThemeRequest request = new ThemeRequest(
                "우테코 레벨 1 탈출",
                "우테코 레벨 1 탈출하는 내용",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(16));

        RestAssured.given().log().all()
                .when().delete("/themes/{id}", 15)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("삭제하고자 하는 테마에 예약이 등록되어 있으면 400 오류를 반환한다.")
    @Test
    void given_when_deleteThemeIdRegisteredReservation_then_statusCodeIsBadRequest() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/5")
                .then().log().all()
                .statusCode(400)
                .body(containsString("예약이 등록된 테마는 제거할 수 없습니다"));
    }

    @DisplayName("테마 등록 시 테마명이 비어있으면 400 오류를 반환한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void given_when_saveThemeWithBlankName_then_statusCodeIsBadRequest(String given) {
        ThemeRequest request = new ThemeRequest(given, "123", "123");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .body(containsString("테마명은 비어있을 수 없습니다."));
    }

    @DisplayName("테마 등록 시 테마 설명이 비어있으면 400 오류를 반환한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void given_when_saveThemeWithBlankDescription_then_statusCodeIsBadRequest(String given) {
        ThemeRequest request = new ThemeRequest("test", given, "123");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .body(containsString("테마 설명은 비어있을 수 없습니다."));
    }

    @DisplayName("테마 등록 시 썸네일이 비어있으면 400 오류를 반환한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void given_when_saveThemeWithBlankThumbNail_then_statusCodeIsBadRequest(String given) {
        ThemeRequest request = new ThemeRequest("test", "123", given);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .body(containsString("썸네일은 비어있을 수 없습니다."));
    }

    @DisplayName("테마 등록 시 썸네일 주소가 올바르지 않을 경우 400 오류를 반환한다.")
    @Test
    void given_when_saveThemeWithInvalidThumbnail_then_statusCodeIsBadRequest() {
        ThemeRequest request = new ThemeRequest("test", "123", "123");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .body(containsString("썸네일 URL 형식이 올바르지 않습니다"));
    }
}
