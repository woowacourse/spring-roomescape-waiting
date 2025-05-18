package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TimeControllerTest {

    @Test
    @DisplayName("/times 요청 시 시간 정보 조회")
    void readReservationTimes() {
        RestAssured.given().log().all()
            .when().get("/times")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("시간 관리 페이지 내에서 시간 추가")
    void createReservationTime() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(getTestParamsWithReservationTime())
            .when().post("/times")
            .then().log().all()
            .statusCode(201);

        RestAssured.given().log().all()
            .when().get("/times")
            .then().log().all()
            .statusCode(200);
    }

    @Test
    @DisplayName("시간 관리 페이지 내에서 예약 삭제")
    void deleteReservationTime() {
        int  id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(getTestParamsWithReservationTime())
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
            .when().delete("/times/" + id)
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .when().get("/times")
            .then().log().all()
            .statusCode(200);
    }

    private Map<String, String> getTestParamsWithReservationTime() {
        return Map.of(
            "startAt", "11:00"
        );
    }
}
