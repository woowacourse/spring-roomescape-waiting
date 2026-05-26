package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.Map;

/**
 * 서비스 정책 검증 인수 테스트
 *
 * data.sql 기준 예약 ID:
 *   1  ~ 40 : Theme 1, CONFIRMED (미래 날짜)
 *  41  ~ 75 : Theme 2, CONFIRMED (미래 날짜)
 *  76       : COMPLETED, 2026-04-26, time_id=1, theme_id=1
 *  77       : COMPLETED, 2026-04-26, time_id=4, theme_id=1
 *  78       : CANCELLED, 2026-04-26, time_id=7, theme_id=2
 *
 * 중복 예약 검증용 기존 예약:
 *   theme_id=1, date=2026-05-16, time_id=3 (CONFIRMED)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PolicyAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    // ── 공통 규칙: 유효하지 않은 입력값(400) ──────────────────────────────────────

    @Test
    @DisplayName("[공통] 예약자 이름이 빈 문자열이면 400을 반환한다.")
    void 빈_이름으로_예약_등록시_400() {
        Map<String, Object> body = Map.of(
                "name", "",
                "date", LocalDate.now().plusDays(1).toString(),
                "timeId", 1,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("[공통] 날짜 없이 예약 요청하면 400을 반환한다.")
    void 날짜_없이_예약_등록시_400() {
        Map<String, Object> body = Map.of(
                "name", "브라운",
                "timeId", 1,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("[공통] 시간 ID 없이 예약 요청하면 400을 반환한다.")
    void 시간ID_없이_예약_등록시_400() {
        Map<String, Object> body = Map.of(
                "name", "브라운",
                "date", LocalDate.now().plusDays(1).toString(),
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    // ── 예약 규칙: 존재하지 않는 예약(404) ────────────────────────────────────────

    @Test
    @DisplayName("[예약] 존재하지 않는 예약을 취소하면 404를 반환한다.")
    void 존재하지_않는_예약_취소시_404() {
        RestAssured.given().log().all()
                .when().patch("/reservations/99999/cancel")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @DisplayName("[예약] 존재하지 않는 예약을 변경하면 404를 반환한다.")
    void 존재하지_않는_예약_변경시_404() {
        Map<String, Object> body = Map.of(
                "date", LocalDate.now().plusDays(3).toString(),
                "timeId", 2,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/reservations/99999")
                .then().log().all()
                .statusCode(404);
    }

    // ── 예약 규칙: 이미 취소/완료된 예약 취소(409) ────────────────────────────────

    // ── 예약 규칙: 과거 날짜·시간(422) ────────────────────────────────────────────

    @Test
    @DisplayName("[예약] 과거 날짜로 예약 등록하면 422를 반환한다.")
    void 과거_날짜로_예약_등록시_422() {
        Map<String, Object> body = Map.of(
                "name", "브라운",
                "date", "2024-01-01",
                "timeId", 1,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("[예약] 과거 날짜로 예약 변경하면 422를 반환한다.")
    void 과거_날짜로_예약_변경시_422() {
        Map<String, Object> body = Map.of(
                "date", "2024-01-01",
                "timeId", 1,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("[예약] 이미 취소된 예약을 취소하면 422를 반환한다.")
    void 이미_취소된_예약_취소시_422() {
        // ID 78: CANCELLED 상태
        RestAssured.given().log().all()
                .when().patch("/reservations/78/cancel")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("[예약] 이미 완료된 예약을 취소하면 422를 반환한다.")
    void 이미_완료된_예약_취소시_422() {
        // ID 76: COMPLETED 상태
        RestAssured.given().log().all()
                .when().patch("/reservations/76/cancel")
                .then().log().all()
                .statusCode(422);
    }

    // ── 예약 규칙: 존재하지 않는 시간 ID로 변경(404) ──────────────────────────────

    @Test
    @DisplayName("[예약] 존재하지 않는 시간 ID로 예약 변경하면 404를 반환한다.")
    void 존재하지_않는_시간ID로_예약_변경시_404() {
        Map<String, Object> body = Map.of(
                "date", LocalDate.now().plusDays(1).toString(),
                "timeId", 99999,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(404);
    }

    // ── 예약 규칙: 중복 예약(409) ──────────────────────────────────────────────────

    @Test
    @DisplayName("[예약] 같은 날짜·시간·테마에 이미 예약이 있으면 409를 반환한다.")
    void 중복_예약_등록시_409() {
        // theme_id=1, date=2026-05-16, time_id=3 은 이미 예약됨
        Map<String, Object> body = Map.of(
                "name", "신규사용자",
                "date", "2026-05-30",
                "timeId", 3,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }

    // ── 예약 규칙: 예약이 존재하는 시간 삭제(422) ──────────────────────────────────

    @Test
    @DisplayName("[시간] 예약이 존재하는 시간을 삭제하면 422를 반환한다.")
    void 예약이_존재하는_시간_삭제시_422() {
        // time_id=1 은 여러 예약에서 사용 중
        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(422);
    }
}
