package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationStatus;

public class WaitlistApiTest extends AcceptanceTest {

    public static final String FUTURE_EIGHT_DATE = LocalDate.now().plusDays(8).toString();

    @Test
    @Sql("/data_relative_dates.sql")
    void 예약_대기_추가_후_삭제() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", FUTURE_EIGHT_DATE);
        params.put("timeId", 9);
        params.put("themeId", 8);

        Integer waitlistId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", is(ReservationStatus.WAITING.name()))
                .body("waitingOrder", is(1))
                .extract().jsonPath().get("id");

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/waitlists/" + waitlistId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 없는_예약대기를_삭제할_수_없다_404() {
        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/waitlists/" + 1)
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @Sql("/data_relative_dates.sql")
    void 예약대기를_삭제할_때_사용자_이름이_일치하지_않으면_403() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", FUTURE_EIGHT_DATE);
        params.put("timeId", 9);
        params.put("themeId", 8);

        Integer waitlistId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", is(ReservationStatus.WAITING.name()))
                .body("waitingOrder", is(1))
                .extract().jsonPath().get("id");

        String other = "브리";
        RestAssured.given().log().all()
                .queryParam("name", other)
                .when().delete("/waitlists/" + waitlistId)
                .then().log().all()
                .statusCode(403);
    }
}
