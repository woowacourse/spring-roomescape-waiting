package roomescape.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import roomescape.AcceptanceTest;

public class ReservationControllerTest extends AcceptanceTest {

    @Test
    void 예약을_생성한다() {
        long timeId = apiFixtureGenerator.createTime("10:00");
        long themeId = apiFixtureGenerator.createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", reservationDate.toString());
        params.put("timeId", timeId);
        params.put("themeId", themeId);
        params.put("amount", 10000);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getLong("id");
    }

    @Test
    void 같은_날짜_및_시간이더라도_테마가_다르면_예약_가능하다() {
        long timeId = apiFixtureGenerator.createTime("10:00");
        long themeId1 = apiFixtureGenerator.createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        long themeId2 = apiFixtureGenerator.createTheme("방탈출2", "다함께 탈출해요 방탈출2.", "https://asdfsdf.sdfssdafdasf");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "로지",
                        "date", reservationDate.toString(),
                        "timeId", timeId,
                        "themeId", themeId1,
                        "amount", 10000
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("로지"));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "러키",
                        "date", reservationDate.toString(),
                        "timeId", timeId,
                        "themeId", themeId2,
                        "amount", 10000
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("러키"));
    }

    @Test
    void 예약을_조회한다() {
        long timeId = apiFixtureGenerator.createTime("10:00");
        long themeId = apiFixtureGenerator.createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        apiFixtureGenerator.createReservation("브라운", reservationDate, timeId, themeId);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem("브라운"));
    }

    @Test
    void 이름에_따른_예약들을_조회할_수_있다() {
        long time10Id = apiFixtureGenerator.createTime("10:00");
        long time11Id = apiFixtureGenerator.createTime("11:00");
        long themeId = apiFixtureGenerator.createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        apiFixtureGenerator.createReservation("브라운", reservationDate, time10Id, themeId);
        apiFixtureGenerator.createReservation("조이", reservationDate, time11Id, themeId);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem("브라운"))
                .body("name", not(hasItem("조이")));
    }

    @Test
    void 예약을_수정한다() {
        long originalTimeId = apiFixtureGenerator.createTime("10:00");
        long changedTimeId = apiFixtureGenerator.createTime("11:00");

        LocalDate originalDate = LocalDate.of(2099, 5, 31);
        LocalDate changedDate = LocalDate.of(2099, 6, 1);

        long themeId = apiFixtureGenerator.createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");

        long reservationId = apiFixtureGenerator.createReservation("브라운", originalDate, originalTimeId, themeId);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("date", changedDate.toString());
        updateParams.put("timeId", changedTimeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(updateParams)
                .when().put("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(200)
                .body("name", is("브라운"))
                .body("date", is(changedDate.toString()));
    }

    @Test
    void 예약을_삭제한다() {
        long timeId = apiFixtureGenerator.createTime("10:00");
        long themeId = apiFixtureGenerator.createTheme("방탈출11", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        long reservationId = apiFixtureGenerator.createReservation("브라운", reservationDate, timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }
}
