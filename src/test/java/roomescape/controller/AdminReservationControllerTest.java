package roomescape.controller;

import static org.hamcrest.Matchers.hasItem;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class AdminReservationControllerTest extends ControllerTestSupport {

    @Test
    void 전체_예약을_조회한다() {
        int brownId = createMember("브라운");
        int rojiId = createMember("로지");
        int timeId1 = createTime("10:00");
        int timeId2 = createTime("11:00");
        int themeId = createTheme("방탈출1", "다함께 탈출해요 방탈출", "https://asdfsdf.sdfs");
        createReservation(brownId, LocalDate.now().plusDays(1).toString(), timeId1, themeId).statusCode(201);
        createReservation(rojiId, LocalDate.now().plusDays(1).toString(), timeId2, themeId).statusCode(201);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem("브라운"));
    }
}
