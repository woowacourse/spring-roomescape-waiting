package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.util.TokenGenerator;

import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationTimeApiControllerTest {

    @Test
    @DisplayName("예약 시간 추가, 조회, 삭제를 정상적으로 수행한다.")
    void ReservationTime_CREATE_READ_DELETE_Success() {
        Map<String, String> time = Map.of(
                "startAt", "13:00"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", TokenGenerator.makeAdminToken())
                .body(time)
                .when().post("/api/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/api/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4));

        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().delete("/api/admin/times/4")
                .then().log().all()
                .statusCode(204);
    }
}
