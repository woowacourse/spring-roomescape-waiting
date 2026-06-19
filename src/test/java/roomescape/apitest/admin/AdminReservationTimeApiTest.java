package roomescape.apitest.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationTimeApiTest {

    @Test
    @DisplayName("관리자는 시간을 등록하고 삭제할 수 있다.")
    void registerAndDeleteReservationTime_Success() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "19:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/admin/times/10")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("시간 등록 시, 시작 시간이 null 이면 400 에러를 반환한다.")
    void registerReservationTime_WhenStartAtIsNull_Return400() {
        Map<String, String> params = new HashMap<>();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400);
    }
}
