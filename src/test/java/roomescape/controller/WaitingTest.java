package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(FixedClockConfig.class)
public class WaitingTest {
    @Test
    @DisplayName("기존 예약이 존재할 때 예약 대기가 성공적으로 되는지 확인한다.")
    void createWaitingTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-06-05");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waitings")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("예약 대기가 성공적으로 취소되는지 확인한다.")
    public void cancelWaitingTest() {
        RestAssured.given().log().all()
                .queryParam("name", "user_d")
                .when().delete("/reservations/waitings/2")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("존재하지 않는 예약 대기를 취소하면 404를 반환한다.")
    public void cancelNonExistentWaitingTest() {
        RestAssured.given().log().all()
                .queryParam("name", "user_d")
                .when().delete("/reservations/waitings/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @DisplayName("지나간 시간에는 예약 대기를 생성할 수 없다.")
    void createPastWaitingTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "녀녕");
        params.put("date", "2026-04-28");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations/waitings")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("이미 시작된 게임의 예약 대기는 취소할 수 없다.")
    public void cancelPastWaitingTest() {
        RestAssured.given().log().all()
                .queryParam("name", "user_d")
                .when().delete("/reservations/waitings/1")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("타인의 예약 대기는 취소할 수 없다.")
    public void cancelOtherWaitingTest() {
        RestAssured.given().log().all()
                .queryParam("name", "녀녕")
                .when().delete("/reservations/waitings/2")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("빈 이름으로 예약 대기를 취소하면 400을 반환한다.")
    public void cancelWaitingWithBlankNameTest() {
        RestAssured.given().log().all()
                .queryParam("name", "")
                .when().delete("/reservations/waitings/2")
                .then().log().all()
                .statusCode(400);
    }
}
