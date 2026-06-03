package roomescape.controller;

import static org.hamcrest.core.Is.is;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class ReservationControllerTest {

    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    @DisplayName("사용자 예약 추가 API")
    @Test
    void 사용자_예약_추가_API() {
        LocalDate date = LocalDate.now();
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", date.plusDays(1));
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("date", is(date.plusDays(1).toString()))
                .body("themeName", is("공포의 저택"))
                .body("themeDescription", is("버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다."))
                .body("themeThumbnailUrl", is("https://picsum.photos/seed/haunted/400/250"))
                .body("time", is("10:00"))
                .body("name", is("브라운"))
                .body("order", is(0));
    }

    @DisplayName("사용자 예약 추가 API - 이상값 예외 테스트")
    @Test
    void API_사용자_예약_추가_예외_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2025");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @DisplayName("사용자 예약 삭제 API")
    @Test
    void 사용자_예약_삭제_API() {
        LocalDate date = LocalDate.now();
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", date.plusDays(1));
        params.put("timeId", 1);
        params.put("themeId", 1);

        final long id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("reservationId");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("reservationId", id)
                .queryParam("name", "브라운")
                .when().delete("/reservations/{reservationId}")
                .then().log().all()
                .statusCode(204);
    }

    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @DisplayName("사용자 예약 조회 API")
    @Test
    void 사용자_예약_조회_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("username", "김철수")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("[0].reservationId", is(1))
                .body("[0].name", is("김철수"))
                .body("[0].date", is("2026-04-29"))
                .body("[0].themeName", is("공포의 저택"))
                .body("[0].themeDescription", is("버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다."))
                .body("[0].themeThumbnailUrl", is("https://picsum.photos/seed/haunted/400/250"))
                .body("[0].time", is("12:00"))
                .body("[0].order", is(0));
    }

    @DisplayName("사용자 예약 추가 - 날짜 형식 예외 테스트")
    @Test
    void 사용자_예약_추가_날짜_형식_예외_테스트() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2024-95-05");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("사용자 예약 추가 - 이름 형식 예외 테스트")
    @Test
    void 사용자_예약_추가_이름_형식_예외_테스트() {
        String longName = "a".repeat(256);

        Map<String, Object> params = new HashMap<>();
        params.put("name", longName);
        params.put("date", "2024-95-05");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @DisplayName("사용자 예약 대기 - 정상 테스트")
    @Test
    void 사용자_예약_대기_정상_테스트() {
        LocalDate date = LocalDate.now();

        // 첫번째 예약
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브");
        params.put("date", date.plusDays(1));
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("브"))
                .body("order", is(0));


        // 두번째 예약
        params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", date.plusDays(1));
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("브라운"))
                .body("order", is(1));
    }

    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    @DisplayName("사용자 예약 대기 삭제 API")
    @Test
    void 사용자_예약_대기_삭제_API() {
        LocalDate date = LocalDate.now();

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", date.plusDays(1));
        params.put("timeId", 1);
        params.put("themeId", 1);
        getReservationId(params);

        params = new HashMap<>();
        params.put("name", "검프");
        params.put("date", date.plusDays(1));
        params.put("timeId", 1);
        params.put("themeId", 1);
        final long id = getReservationId(params);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("reservationId", id)
                .queryParam("name", "검프")
                .when().delete("/reservations/{reservationId}")
                .then().log().all()
                .statusCode(204);
    }

    private long getReservationId(Map<String, Object> params) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("reservationId");
    }
}
