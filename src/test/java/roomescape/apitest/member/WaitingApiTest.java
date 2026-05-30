package roomescape.apitest.member;

import static roomescape.config.FixedClockConfig.FUTURE_DATE;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingApiTest {
    private final Long waitingId = 1L;
    private final String userName = "로운";
    private final Long timeId = 5L;
    private final Long themeId = 1L;

    @Test
    void 예약_대기_생성_API() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", userName);
        reservation.put("date", FUTURE_DATE);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 예약_대기_취소_API() {
        String userName = "토리";
        RestAssured.given().log().all()
                .when().delete("/waitings/" + waitingId + "?name=" + userName)
                .then().log().all()
                .statusCode(204);
    }
}
