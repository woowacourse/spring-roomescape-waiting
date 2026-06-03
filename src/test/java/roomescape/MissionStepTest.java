package roomescape;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    public static final String FUTURE_FIRST_DATE = LocalDate.now().plusDays(1).toString();

    @Test
    void 예약_조회() {
        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(0));
    }

    @Test
    void 예약_추가_및_삭제() {
        Map<String, String> timeParams = new HashMap<>();
        timeParams.put("startAt", "15:40");

        Integer timeId = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(timeParams)
            .when().post("/times")
            .then().log().all()
            .statusCode(201)
            .extract().jsonPath().get("id");

        Map<String, String> themeParams = new HashMap<>();
        themeParams.put("name", "공포");
        themeParams.put("description", "무서운 테마");
        themeParams.put("thumbnailImageUrl", "https://example.com/horror.jpg");

        Integer themeId = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(themeParams)
            .when().post("/themes")
            .then().log().all()
            .statusCode(201)
            .extract().jsonPath().get("id");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", FUTURE_FIRST_DATE);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("id", is(1));

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(1));

        RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("size()", is(0));
    }
}
