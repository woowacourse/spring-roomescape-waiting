package roomescape.apitest.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static roomescape.config.FixedClockConfig.FUTURE_DATE;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingApiTest {

    private static final Long timeId = 5L;
    private static final Long themeId = 1L;
    private static final Long waitingId = 1L;

    @Test
    void 예약_대기_생성_API() {
        String newUser = "로운";
        Long createdId = createWaiting(newUser, FUTURE_DATE, timeId, themeId);
        assertThat(createdId).isNotNull();

        JsonPath jsonPath = getReservationByUserName(newUser);
        List<Map<String, Object>> details = jsonPath.getList("reservationDetailResponses");

        assertThat(details).hasSize(1);
        Map<String, Object> only = details.getFirst();
        assertThat(only.get("status")).isEqualTo("WAITING");
        assertThat(only.get("sequence")).isEqualTo(2);
        assertThat(only.get("date")).isEqualTo(FUTURE_DATE);
    }

    @Test
    void 예약이_없는_슬롯에_대기를_신청하면_예외가_발생한다() {
        String newUser = "로운";
        Long emptySlotTimeId = 6L;

        Map<String, Object> body = new HashMap<>();
        body.put("name", newUser);
        body.put("date", FUTURE_DATE);
        body.put("timeId", emptySlotTimeId);
        body.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(422)
                .body("message", containsString("예약이 존재하지 않으면 예약 대기를 생성할 수 없습니다."));

        assertThat(getReservationByUserName(newUser).getList("reservationDetailResponses")).isEmpty();
    }

    @Test
    void 같은_사용자가_같은_슬롯에_중복으로_대기를_신청하면_예외가_발생한다() {
        String existingUser = "토리";

        Map<String, Object> body = new HashMap<>();
        body.put("name", existingUser);
        body.put("date", FUTURE_DATE);
        body.put("timeId", timeId);
        body.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(422)
                .body("message", containsString("예약 대기는 중복으로 생성할 수 없습니다."));

        assertThat(waitingCount(existingUser)).isEqualTo(1);
    }

    @Test
    void 본인이_예약한_슬롯에_대기를_신청하면_예외가_발생한다() {
        String owner = "브라운";

        Map<String, Object> body = new HashMap<>();
        body.put("name", owner);
        body.put("date", FUTURE_DATE);
        body.put("timeId", timeId);
        body.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(422)
                .body("message", containsString("본인이 이미 예약한 시간에는 대기를 신청할 수 없습니다."));

        assertThat(waitingCount(owner)).isEqualTo(0);
    }

    @Test
    void 예약_대기_취소_API() {
        String userName = "토리";

        RestAssured.given().log().all()
                .when().delete("/waitings/" + waitingId + "?name=" + userName)
                .then().log().all()
                .statusCode(204);

        assertThat(waitingCount(userName)).isEqualTo(0);
    }

    @Test
    void 다른_사람의_대기를_삭제는_예외가_발생한다() {
        String other = "로운";

        RestAssured.given().log().all()
                .when().delete("/waitings/" + waitingId + "?name=" + other)
                .then().log().all()
                .statusCode(403)
                .body("message", containsString("다른 사람의 예약 대기는 취소할 수 없습니다."));

        assertThat(waitingCount("토리")).isEqualTo(1);
    }

    private Long createWaiting(String name, String date, Long timeId, Long themeId) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("date", date);
        body.put("timeId", timeId);
        body.put("themeId", themeId);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private JsonPath getReservationByUserName(String userName) {
        return RestAssured.given().log().all()
                .when().get("/reservations?userName=" + userName)
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath();
    }

    private Long waitingCount(String userName) {
        List<Map<String, Object>> responses = getReservationByUserName(userName)
                .getList("reservationDetailResponses");

        return responses.stream()
                .filter(r -> r.get("status").equals("WAITING"))
                .count();
    }
}
