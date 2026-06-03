package roomescape.accaptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

/**
 * 서비스 정책 검증 인수 테스트
 * acceptance-reset.sql 기준 예약 ID:
 *   1  ~ 40 : Theme 1, CONFIRMED (미래 날짜)
 *  41  ~ 75 : Theme 2, CONFIRMED (미래 날짜)
 *  76       : COMPLETED, theme_slot_id=76 (2026-04-26, time_id=1, theme_id=1)
 *  77       : COMPLETED, theme_slot_id=77 (2026-04-26, time_id=4, theme_id=1)
 *  78       : CANCELLED, theme_slot_id=78 (2026-04-26, time_id=7, theme_id=2)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/acceptance-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PolicyAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    // 성공 시나리오

    @Test
    @DisplayName("빈 슬롯에 예약하면 CONFIRMED 상태로 즉시 확정된다.")
    void 빈_슬롯_예약시_CONFIRMED로_생성된다() {
        long themeSlotId = 새_슬롯_조회(1);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "브라운", "themeSlotId", themeSlotId))
                .when().post("/reservations")
                .then().statusCode(201)
                .body("name", equalTo("브라운"))
                .body("status", equalTo("CONFIRMED"));
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 신청하면 PENDING 대기 상태로 생성된다.")
    void 이미_예약된_슬롯_신청시_PENDING으로_생성된다() {
        long themeSlotId = 새_슬롯_조회(1);

        RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "첫번째", "themeSlotId", themeSlotId))
                .when().post("/reservations")
                .then().statusCode(201)
                .body("status", equalTo("CONFIRMED"));

        RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "두번째", "themeSlotId", themeSlotId))
                .when().post("/reservations")
                .then().statusCode(201)
                .body("status", equalTo("PENDING"));
    }

    @Test
    @DisplayName("CONFIRMED 예약 취소 시 첫 번째 PENDING 대기자가 자동으로 CONFIRMED로 승격된다.")
    void CONFIRMED_취소시_대기자가_자동_승격된다() {
        long themeSlotId = 새_슬롯_조회(1);

        long confirmedId = RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "첫번째", "themeSlotId", themeSlotId))
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().contentType(ContentType.JSON)
                .body(Map.of("name", "두번째", "themeSlotId", themeSlotId))
                .when().post("/reservations")
                .then().statusCode(201);

        RestAssured.given()
                .when().patch("/reservations/" + confirmedId + "/cancel")
                .then().statusCode(204);

        RestAssured.given()
                .when().get("/reservations?name=두번째")
                .then().statusCode(200);
    }

    // 과거 날짜로 위반

    @Test
    @DisplayName("과거 날짜로 예약 등록하면 422를 반환한다.")
    void 과거_날짜로_예약_등록시_422() {
        // theme_slot_id=76: 2026-04-26 (과거 날짜)
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "브라운", "themeSlotId", 76))
                .when().post("/reservations")
                .then().statusCode(422);
    }

    @Test
    @DisplayName("과거 날짜로 예약 변경하면 422를 반환한다.")
    void 과거_날짜로_예약_변경시_422() {
        // theme_slot_id=76: 2026-04-26 (과거 날짜)
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("themeSlotId", 76))
                .when().patch("/reservations/1")
                .then().statusCode(422);
    }

    // 예약 상태 위반

    @Test
    @DisplayName("이미 취소된 예약을 취소하면 422를 반환한다.")
    void 이미_취소된_예약_취소시_422() {
        // reservation_id=78: CANCELLED 상태
        RestAssured.given()
                .when().patch("/reservations/78/cancel")
                .then().statusCode(422);
    }

    @Test
    @DisplayName("이미 완료된 예약을 취소하면 422를 반환한다.")
    void 이미_완료된_예약_취소시_422() {
        // reservation_id=76: COMPLETED 상태
        RestAssured.given()
                .when().patch("/reservations/76/cancel")
                .then().statusCode(422);
    }

    // 404

    @Test
    @DisplayName("존재하지 않는 예약을 취소하면 404를 반환한다.")
    void 존재하지_않는_예약_취소시_404() {
        RestAssured.given()
                .when().patch("/reservations/99999/cancel")
                .then().statusCode(404);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 변경하면 404를 반환한다.")
    void 존재하지_않는_예약_변경시_404() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("themeSlotId", 2))
                .when().patch("/reservations/99999")
                .then().statusCode(404);
    }

    @Test
    @DisplayName("존재하지 않는 테마 슬롯 ID로 예약 변경하면 404를 반환한다.")
    void 존재하지_않는_테마슬롯ID로_예약_변경시_404() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("themeSlotId", 99999))
                .when().patch("/reservations/1")
                .then().statusCode(404);
    }

    // 409

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 예약하면 409를 반환한다.")
    void 중복_예약_등록시_409() {
        long themeSlotId = 새_슬롯_조회(1);
        Map<String, Object> body = Map.of("name", "브라운", "themeSlotId", themeSlotId);

        RestAssured.given().contentType(ContentType.JSON).body(body)
                .when().post("/reservations")
                .then().statusCode(201);

        RestAssured.given().contentType(ContentType.JSON).body(body)
                .when().post("/reservations")
                .then().statusCode(409);
    }

    // 422

    @Test
    @DisplayName("예약이 존재하는 시간을 삭제하면 422를 반환한다.")
    void 예약이_존재하는_시간_삭제시_422() {
        // time_id=1 은 여러 예약에서 사용 중
        RestAssured.given()
                .when().delete("/times/1")
                .then().statusCode(422);
    }

    private long 새_슬롯_조회(int themeId) {
        String futureDate = LocalDate.now().plusMonths(6).toString();
        return RestAssured.given()
                .when().get("/times?themeId=" + themeId + "&date=" + futureDate)
                .then().statusCode(200)
                .extract().jsonPath().getLong("[0].id");
    }
}
