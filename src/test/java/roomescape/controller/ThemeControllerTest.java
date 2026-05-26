package roomescape.controller;

import static org.hamcrest.core.Is.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerTest {

    @Test
    @DisplayName("전체 테마 조회 API")
    void 전체_테마_조회_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()",is(4))
                .body("[0].id",is(1))
                .body("[0].name",is("공포의 저택"))
                .body("[0].description",is("버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다."))
                .body("[0].thumbnailUrl",is("https://picsum.photos/seed/haunted/400/250"));
    }

    @Test
    @DisplayName("Top 10 테마 조회 API")
    void Top_10_테마_조회_API() {
        Map<String,Object> params = new HashMap<>();
        params.put("size", "10");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .params(params)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("[0].id",is(1))
                .body("[0].name",is("공포의 저택"))
                .body("[0].description",is("버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다."))
                .body("[0].thumbnailUrl",is("https://picsum.photos/seed/haunted/400/250"));
    }

    @Test
    @DisplayName("특정 id의 테마 시간 조회_API")
    void 특정_id의_테마_시간_조회_API() {

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("id", 1)
                .queryParam("date", "2026-04-29")
                .when().get("/themes/{id}/available-times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(13))
                .body("[0].id", is(1))
                .body("[0].startAt", is("10:00"))
                .body("[0].isAvailable", is(true))
                .body("[2].id", is(3))
                .body("[2].startAt", is("12:00"))
                .body("[2].isAvailable", is(false));
    }

    @Test
    @DisplayName("특정 id의 테마 시간 조회_API - 이상값 예외 테스트")
    void 특정_id의_테마_시간_조회_API_예외_테스트() {

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("id", "ㅇㄴ")
                .queryParam("date", "2026-04-29")
                .when().get("/themes/{id}/available-times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("특정 id의 테마 시간 조회_API - 정상 테스트")
    void 특정_id의_테마_시간_조회_API_정상_테스트() {

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("id", 1)
                .queryParam("date", "2026-04-29")
                .when().get("/themes/{id}/available-times")
                .then().log().all()
                .statusCode(200);
    }
}
