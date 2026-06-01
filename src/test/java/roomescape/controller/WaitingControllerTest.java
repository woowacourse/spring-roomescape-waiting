package roomescape.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import roomescape.AcceptanceTest;

public class WaitingControllerTest extends AcceptanceTest {

    @Test
    void 대기를_신청한다() {
        long timeId = apiFixtureGenerator.createTime("10:00");
        long themeId = apiFixtureGenerator.createTheme("테마1", "적어도 10글자 이상이어야 합니다.", "https://dsf.sdaf");
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        apiFixtureGenerator.createReservation("기존예약자", reservationDate, timeId, themeId);

        Map<String, Object> params = Map.of(
                "date", reservationDate.toString(),
                "timeId", timeId,
                "themeId", themeId,
                "name", "대기신청자"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("대기신청자"));
    }

    @Test
    void 대기_목록을_조회한다() {
        long timeId = apiFixtureGenerator.createTime("10:00");
        long themeId = apiFixtureGenerator.createTheme("테마1", "적어도 10글자 이상이어야 합니다.", "https://dsf.sdaf");
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        apiFixtureGenerator.createReservation("기존예약자", reservationDate, timeId, themeId);

        String name = "대기신청자";
        apiFixtureGenerator.createWaiting(name, reservationDate, timeId, themeId);

        RestAssured.given().log().all()
                .queryParam("name", name)
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem(name))
                .body("rank", hasItem(1));
    }

    @Test
    void 대기를_취소한다() {
        long timeId = apiFixtureGenerator.createTime("10:00");
        long themeId = apiFixtureGenerator.createTheme("테마1", "적어도 10글자 이상이어야 합니다.", "https://dsf.sdaf");
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        apiFixtureGenerator.createReservation("기존예약자", reservationDate, timeId, themeId);

        String name = "대기신청자";
        long waitingId = apiFixtureGenerator.createWaiting(name, reservationDate, timeId, themeId);

        RestAssured.given().log().all()
                .queryParam("name", name)
                .when().delete("/waitings/" + waitingId)
                .then().log().all()
                .statusCode(204);
    }
}
