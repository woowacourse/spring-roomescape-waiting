package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.DatabaseInitializer;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationControllerTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 예약을_추가하면_결제_대기_정보를_반환한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");

        createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId, themeId)
                .statusCode(201)
                .body("reservationId", notNullValue())
                .body("orderId", notNullValue())
                .body("amount", is(10000))
                .header("Location", "/reservations/1");
    }

    @Test
    void 같은_날짜_및_시간이더라도_테마가_다르면_예약이_가능하다() {
        int timeId = createTime("10:00");
        int themeId1 = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        int themeId2 = createTheme("방탈출2", "다함께 탈출해요 방탈출2", "https://asdfsdf.sdfssdafdasf");

        createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId, themeId1).statusCode(201);
        createReservation("로지", LocalDate.now().plusDays(1).toString(), timeId, themeId2).statusCode(201);
    }

    @Test
    void 날짜_형식이_잘못되면_400을_반환한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "브라운", "date", "잘못된날짜", "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 중복_예약을_하면_409를_반환한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        String date = LocalDate.now().plusDays(1).toString();
        createReservation("브라운", date, timeId, themeId).statusCode(201);

        createReservation("로지", date, timeId, themeId).statusCode(409);
    }

    @Test
    void 지나간_날짜로_예약하면_422를_반환한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        createReservation("브라운", "2026-04-01", timeId, themeId).statusCode(422);
    }

    @Test
    void 내_예약_목록을_조회한다() {
        int timeId1 = createTime("10:00");
        int timeId2 = createTime("11:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        String name = createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId1, themeId)
                .statusCode(201)
                .extract()
                .path("name");
        createReservation("로지", LocalDate.now().plusDays(1).toString(), timeId2, themeId).statusCode(201);
        createReservationWaiting("브라운", LocalDate.now().plusDays(1), timeId2, themeId).statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations?name=" + name)
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem("브라운"))
                .body("status", hasItem("WAITING"));
    }

    @Test
    void 예약_날짜_시간을_변경한다() {
        int timeId1 = createTime("10:00");
        int timeId2 = createTime("11:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        int reservationId = createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId1, themeId)
                .statusCode(201)
                .extract().path("reservationId");

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
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        int reservationId = createReservation("브라운", LocalDate.now().plusDays(1).toString(), timeId, themeId)
                .statusCode(201)
                .extract().path("reservationId");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("date", "2026-04-01", "timeId", timeId))
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(422);
    }

    @Test
    void 중복된_날짜_시간으로_변경하면_409를_반환한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        String date = LocalDate.now().plusDays(1).toString();
        createReservation("브라운", date, timeId, themeId).statusCode(201);

        int reservationId2 = createReservation("로지", LocalDate.now().plusDays(2).toString(), timeId, themeId)
                .statusCode(201)
                .extract().path("reservationId");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("date", date, "timeId", timeId))
                .when().patch("/reservations/" + reservationId2)
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 예약을_삭제한다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출11", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        int reservationId = createReservation("브라운", LocalDate.now().plusDays(2).toString(), timeId, themeId)
                .statusCode(201)
                .extract().path("reservationId");

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약을_취소하면_1순위_대기가_예약으로_자동_전환된다() {
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");

        String date = LocalDate.now().plusDays(2).toString();

        int reservationId = createReservation("브라운", date, timeId, themeId)
                .statusCode(201)
                .extract().path("reservationId");

        createReservationWaiting("맥스", LocalDate.parse(date), timeId, themeId)
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations?name=맥스")
                .then().log().all()
                .statusCode(200)
                .body("status", hasItem("RESERVED"))
                .body("status", not(hasItem("WAITING")));
    }

    private int createTime(String startAt) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private int createTheme(String name, String description, String thumbnail) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "description", description, "thumbnail", thumbnail))
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private ValidatableResponse createReservation(String name, String date, int timeId, int themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "date", date, "timeId", timeId, "themeId", themeId))
                .when().post("/reservations")
                .then().log().all();
    }

    private ValidatableResponse createReservationWaiting(String name, LocalDate reservationDate, int timeId, int themeId) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "reservationDate", reservationDate, "timeId", timeId, "themeId", themeId))
                .when().post("/waitings")
                .then().log().all();
    }
}
