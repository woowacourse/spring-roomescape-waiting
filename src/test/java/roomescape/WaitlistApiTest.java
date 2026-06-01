package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitlistApiTest {

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
                .extract().jsonPath().get("id");

        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().delete("/waitlists/" + waitlistId)
                .then().log().all()
                .statusCode(204);
    }
}
