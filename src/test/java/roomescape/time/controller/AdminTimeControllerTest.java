package roomescape.time.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminTimeControllerTest {

    @Test
    void 시간_추가_성공() {
        Map<String, Object> params = new HashMap<>();
        params.put("startAt", "21:00");

        RestAssured.given().log().all()
                .body(params)
                .contentType(ContentType.JSON)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 시간_삭제_성공() {
        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(204);
    }
}