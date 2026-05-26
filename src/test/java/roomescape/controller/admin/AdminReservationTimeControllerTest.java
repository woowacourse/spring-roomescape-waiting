package roomescape.controller.admin;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminReservationTimeControllerTest {

    @Test
    void 시간_관리_API() {
        Map<String, Object> request = new HashMap<>();
        request.put("startAt", "22:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/times")
                .then().statusCode(200);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10));

        RestAssured.given().log().all()
                .when().delete("/admin/times/10")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 이미_예약이_존재하는_시간은_삭제할_수_없다() {
        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("해당 시간에 예약이 존재하여 삭제할 수 없습니다."));
    }
}
