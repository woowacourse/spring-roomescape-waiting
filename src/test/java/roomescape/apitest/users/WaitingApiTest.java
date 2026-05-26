package roomescape.apitest.users;

import static roomescape.config.FixedClockConfig.FUTURE_DATE;
import static roomescape.config.FixedClockConfig.NOW_TIME;
import static roomescape.config.FixedClockConfig.TODAY;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingApiTest {
    private final String userName = "브라운";
    private final Long timeId = 5L;
    private final Long themeId = 1L;
    private final LocalDateTime createdAt =  LocalDateTime.of(
            LocalDate.parse(TODAY),
            LocalTime.parse(NOW_TIME)
    );
    @Test
    void 예약_대기_생성_API(){
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", userName);
        reservation.put("date", FUTURE_DATE);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);
        reservation.put("createdAt", createdAt);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201);
    }
}
