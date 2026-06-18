package roomescape.controller;

import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ReservationControllerTest extends ControllerTestSupport {

    @Test
    void 예약을_추가한다() {
        int memberId = createMember("브라운");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");

        createReservation(memberId, LocalDate.now().plusDays(1).toString(), timeId, themeId)
                .statusCode(201)
                .body("id", notNullValue())
                .header("Location", "/reservations/1");
    }

    @Test
    void 같은_날짜_및_시간이더라도_테마가_다르면_예약이_가능하다() {
        int memberId1 = createMember("브라운");
        int memberId2 = createMember("로지");
        int timeId = createTime("10:00");
        int themeId1 = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        int themeId2 = createTheme("방탈출2", "다함께 탈출해요 방탈출2", "https://asdfsdf.sdfssdafdasf");

        createReservation(memberId1, LocalDate.now().plusDays(1).toString(), timeId, themeId1).statusCode(201);
        createReservation(memberId2, LocalDate.now().plusDays(1).toString(), timeId, themeId2).statusCode(201);
    }

    @Test
    void 날짜_형식이_잘못되면_400을_반환한다() {
        int memberId = createMember("브라운");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("memberId", memberId, "date", "잘못된날짜", "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 중복_예약을_하면_409를_반환한다() {
        int memberId1 = createMember("브라운");
        int memberId2 = createMember("로지");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        String date = LocalDate.now().plusDays(1).toString();
        createReservation(memberId1, date, timeId, themeId).statusCode(201);

        createReservation(memberId2, date, timeId, themeId).statusCode(409);
    }

    @Test
    void 지나간_날짜로_예약하면_422를_반환한다() {
        int memberId = createMember("브라운");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        createReservation(memberId, "2026-04-01", timeId, themeId).statusCode(422);
    }

    @Test
    void 내_예약_목록을_조회한다() {
        int brownId = createMember("브라운");
        int rojiId = createMember("로지");
        int timeId1 = createTime("10:00");
        int timeId2 = createTime("11:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        createReservation(brownId, LocalDate.now().plusDays(1).toString(), timeId1, themeId).statusCode(201);
        createReservation(rojiId, LocalDate.now().plusDays(1).toString(), timeId2, themeId).statusCode(201);
        createReservationWaiting(brownId, LocalDate.now().plusDays(1), timeId2, themeId).statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations-mine?memberId=" + brownId)
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem("브라운"))
                .body("status", hasItem("WAITING"));
    }

    @Test
    void 예약_날짜_시간을_변경한다() {
        int memberId = createMember("브라운");
        int timeId1 = createTime("10:00");
        int timeId2 = createTime("11:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        int reservationId = createReservation(memberId, LocalDate.now().plusDays(1).toString(), timeId1, themeId)
                .statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("date", LocalDate.now().plusDays(2).toString(), "timeId", timeId2))
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(200)
                .body("date", is(LocalDate.now().plusDays(2).toString()));
    }

    @Test
    void 존재하지_않는_예약을_변경하면_404를_반환한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("date", LocalDate.now().plusDays(1).toString(), "timeId", 1))
                .when().patch("/reservations/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 지나간_날짜로_변경하면_422를_반환한다() {
        int memberId = createMember("브라운");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        int reservationId = createReservation(memberId, LocalDate.now().plusDays(1).toString(), timeId, themeId)
                .statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("date", "2026-04-01", "timeId", timeId))
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 중복된_날짜_시간으로_변경하면_409를_반환한다() {
        int memberId1 = createMember("브라운");
        int memberId2 = createMember("로지");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        String date = LocalDate.now().plusDays(1).toString();
        createReservation(memberId1, date, timeId, themeId).statusCode(201);

        int reservationId2 = createReservation(memberId2, LocalDate.now().plusDays(2).toString(), timeId, themeId)
                .statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("date", date, "timeId", timeId))
                .when().patch("/reservations/" + reservationId2)
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 존재하지_않는_예약_취소_시_404를_반환한다() {
        RestAssured.given().log().all()
                .when().delete("/reservations/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약을_삭제한다() {
        int memberId = createMember("브라운");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출11", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        int reservationId = createReservation(memberId, LocalDate.now().plusDays(1).toString(), timeId, themeId)
                .statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }
}
