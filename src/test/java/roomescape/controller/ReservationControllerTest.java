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
        long timeId = createTime("10:00");
        long themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", reservationDate.toString());
        params.put("timeId", timeId);
        params.put("themeId", themeId);

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
        long timeId = createTime("10:00");
        long themeId1 = createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        long themeId2 = createTheme("방탈출2", "다함께 탈출해요 방탈출2.", "https://asdfsdf.sdfssdafdasf");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "로지",
                        "date", reservationDate.toString(),
                        "timeId", timeId,
                        "themeId", themeId1
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
                        "themeId", themeId2
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("러키"));
    }

    @Test
    void 예약을_조회한다() {
        long timeId = createTime("10:00");
        long themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        createReservation("브라운", reservationDate.toString(), timeId, themeId);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem("브라운"));
    }

    @Test
    void 이름에_따른_예약들을_조회할_수_있다() {
        long time10Id = createTime("10:00");
        long time11Id = createTime("11:00");
        long themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        createReservation("브라운", reservationDate.toString(), time10Id, themeId);
        createReservation("조이", reservationDate.toString(), time11Id, themeId);

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
        long timeId = createTime("10:00");
        long themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);
        long reservationId = createReservation("브라운", reservationDate.toString(), timeId, themeId);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("name", "조이");
        updateParams.put("date", reservationDate.toString());
        updateParams.put("timeId", timeId);
        updateParams.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(updateParams)
                .when().put("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(200)
                .body("name", is("조이"));
    }

    @Test
    void 예약을_삭제한다() {
        long timeId = createTime("10:00");
        long themeId = createTheme("방탈출11", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");
        LocalDate reservationDate = LocalDate.of(2099, 5, 31);

        long reservationId = createReservation("브라운", reservationDate.toString(), timeId, themeId);

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }

    private long createTime(String startAt) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private long createTheme(String name, String description, String thumbnail) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        params.put("thumbnail", thumbnail);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private long createReservation(String name, String date, long timeId, long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        return RestAssured.given().log().all()
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
}
