package roomescape.controller;

import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReservationWaitingControllerTest extends ControllerTestSupport {

    @Test
    void 예약_대기를_추가한다() {
        int rojiId = createMember("로지");
        int brownId = createMember("브라운");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        createReservation(rojiId, LocalDate.now().plusDays(1).toString(), timeId, themeId).statusCode(201);

        createReservationWaiting(brownId, LocalDate.now().plusDays(1), timeId, themeId)
                .statusCode(201)
                .body("id", notNullValue())
                .header("Location", "/waitings/1");
    }

    @Test
    void 예약이_없는_슬롯에_대기를_신청하면_400을_반환한다() {
        int brownId = createMember("브라운");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");

        createReservationWaiting(brownId, LocalDate.now().plusDays(1), timeId, themeId)
                .statusCode(404);
    }

    @Test
    void 동일한_슬롯에_중복_대기를_신청하면_409를_반환한다() {
        int rojiId = createMember("로지");
        int brownId = createMember("브라운");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "설명", "https://asdfsdf.sdfs");
        createReservation(rojiId, LocalDate.now().plusDays(1).toString(), timeId, themeId).statusCode(201);
        createReservationWaiting(brownId, LocalDate.now().plusDays(1), timeId, themeId).statusCode(201);

        createReservationWaiting(brownId, LocalDate.now().plusDays(1), timeId, themeId)
                .statusCode(409);
    }

    @Test
    void 예약_대기를_취소한다() {
        int rojiId = createMember("로지");
        int brownId = createMember("브라운");
        int timeId = createTime("10:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        createReservation(rojiId, LocalDate.now().plusDays(1).toString(), timeId, themeId).statusCode(201);
        int waitingId = createReservationWaiting(brownId, LocalDate.now().plusDays(1), timeId, themeId)
                .statusCode(201)
                .extract().path("id");

        RestAssured.given().log().all()
                .when().delete("/waitings/" + waitingId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 존재하지_않는_대기를_취소하면_404를_반환한다() {
        RestAssured.given().log().all()
                .when().delete("/waitings/999")
                .then().log().all()
                .statusCode(404);
    }
}
