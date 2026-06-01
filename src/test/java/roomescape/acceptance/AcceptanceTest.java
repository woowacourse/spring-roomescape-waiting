package roomescape.acceptance;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AcceptanceTest {

    @LocalServerPort
    private int port;
    private long themeId;
    private long timeId;
    private final String date = "2099-12-31";

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        themeId = 테마_생성("테스트 테마");
        timeId = 시간_슬롯_생성("10:00");
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 예약하면 대기 번호가 순차적으로 부여된다")
    void 예약_대기_번호_부여_테스트() {
        예약_생성("사용자A", date, timeId, themeId, 0); // 예약 확정
        예약_생성("사용자B", date, timeId, themeId, 1);
        예약_생성("사용자C", date, timeId, themeId, 2);
    }

    @Test
    @DisplayName("확정된 예약이 취소되면 대기 순번이 자동으로 앞당겨진다")
    void 예약_취소_시_대기_순번_갱신_테스트() {
        long idA = 예약_생성("사용자A", date, timeId, themeId, 0);
        예약_생성("사용자B", date, timeId, themeId, 1);
        예약_생성("사용자C", date, timeId, themeId, 2);

        예약_취소("사용자A", idA);

        내_예약_대기순번_확인("사용자B", 0);
        내_예약_대기순번_확인("사용자C", 1);
    }

    @Test
    @DisplayName("대기 중인 예약을 취소하면 목록에서 정상적으로 제거된다")
    void 대기_예약_취소_테스트() {
        예약_생성("사용자A", date, timeId, themeId, 0);
        long idB = 예약_생성("사용자B", date, timeId, themeId, 1);

        예약_취소("사용자B", idB);

        내_예약_개수_확인("사용자B", 0);
    }

    @Test
    @DisplayName("본인이 이미 예약 또는 대기 중인 슬롯에 다시 예약하면 실패한다")
    void 중복_예약_대기_실패_테스트() {
        예약_생성("사용자A", date, timeId, themeId, 0);

        예약_생성_실패("사용자A", date, timeId, themeId, 409);
    }

    @Test
    @DisplayName("본인의 예약이 아닌 예약을 취소하려 하면 실패한다")
    void 타인_예약_취소_실패_테스트() {
        long idA = 예약_생성("사용자A", date, timeId, themeId, 0);

        예약_취소_실패("사용자B", idA, 403);
    }

    private static long 테마_생성(String themeName) {
        Map<String, String> themeRequest = Map.of(
                "name", themeName,
                "description", "테스트 설명",
                "thumbnailUrl", "https://example.com/thumb.jpg"
        );
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    private static long 시간_슬롯_생성(String time) {
        Map<String, String> timeRequest = Map.of("startAt", time);
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getLong("id");
    }

    private static long 예약_생성(String username, String date, long timeId, long themeId, int expectedWaitingOrder) {
        Map<String, Object> reservationRequest = Map.of(
                "name", username,
                "date", date,
                "timeId", timeId,
                "themeId", themeId
        );
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body("waitingOrder", is(expectedWaitingOrder))
                .extract().jsonPath().getLong("id");
    }

    private static void 예약_생성_실패(String username, String date, long timeId, long themeId, int expectedStatusCode) {
        Map<String, Object> reservationRequest = Map.of(
                "name", username,
                "date", date,
                "timeId", timeId,
                "themeId", themeId
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/user/reservations")
                .then().log().all()
                .statusCode(expectedStatusCode);
    }

    private static void 내_예약_대기순번_확인(String username, int expectedWaitingOrder) {
        RestAssured.given().log().all()
                .param("name", username)
                .when().get("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].waitingOrder", is(expectedWaitingOrder));
    }

    private static void 예약_취소(String username, long reservationId) {
        RestAssured.given().log().all()
                .param("name", username)
                .when().delete("/user/reservations/" + reservationId)
                .then().log().all()
                .statusCode(204);
    }

    private static void 예약_취소_실패(String username, long reservationId, int expectedStatusCode) {
        RestAssured.given().log().all()
                .param("name", username)
                .when().delete("/user/reservations/" + reservationId)
                .then().log().all()
                .statusCode(expectedStatusCode);
    }

    private static ExtractableResponse<Response> 내_예약_목록_조회(String username) {
        return RestAssured.given().log().all()
                .param("name", username)
                .when().get("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .extract();
    }

    private static void 내_예약_개수_확인(String username, int expectedSize) {
        RestAssured.given().log().all()
                .param("name", username)
                .when().get("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(expectedSize));
    }
}
