package roomescape.apitest.member;

import static roomescape.common.config.FixedClockConfig.FUTURE_DATE;

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
public class WaitingApiTest {
    private final Long waitingId = 1L;
    private final String userName = "브리";
    private final Long timeId = 5L;
    private final Long themeId = 1L;

    @Test
    @DisplayName("사용자는 예약 대기를 등록할 수 있다.")
    void registerWaiting_Success() {
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
    @DisplayName("사용자는 본인의 예약 대기를 취소할 수 있다.")
    void deleteWaiting_Success() {
        String userName = "토리";
        RestAssured.given().log().all()
                .when().delete("/waitings/" + waitingId + "?name=" + userName)
                .then().log().all()
                .statusCode(204);
    }
}
