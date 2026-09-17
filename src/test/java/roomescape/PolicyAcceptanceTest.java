package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;

/**
 * 서비스 정책 검증 인수 테스트
 *
 * data.sql 기준 예약 ID:
 *   1  ~ 40 : Theme 1, CONFIRMED (미래 날짜)
 *  41  ~ 75 : Theme 2, CONFIRMED (미래 날짜)
 *  76       : COMPLETED, theme_slot_id=76 (2026-04-26, time_id=1, theme_id=1)
 *  77       : COMPLETED, theme_slot_id=77 (2026-04-26, time_id=4, theme_id=1)
 *  78       : CANCELLED, theme_slot_id=78 (2026-04-26, time_id=7, theme_id=2)
 *
 * 중복 예약 검증용 기존 예약:
 *   theme_slot_id=34 (theme_id=1, date=2028-05-27, time_id=3, CONFIRMED)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PolicyAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
                "themeSlotId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("[공통] 테마 슬롯 ID 없이 예약 요청하면 400을 반환한다.")
    void 테마슬롯ID_없이_예약_등록시_400() {
        Map<String, Object> body = Map.of(
                "name", "브라운"
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
                .queryParam("name", "브라운")
                .when().patch("/reservations/99999/cancel")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @DisplayName("[예약] 존재하지 않는 예약을 변경하면 404를 반환한다.")
    void 존재하지_않는_예약_변경시_404() {
        Map<String, Object> body = Map.of(
                "themeSlotId", 2
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
                "themeSlotId", 76
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
                "themeSlotId", 76
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
                .queryParam("name", "과거게스트")
                .when().patch("/reservations/78/cancel")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("[예약] 이미 완료된 예약을 취소하면 422를 반환한다.")
    void 이미_완료된_예약_취소시_422() {
        // ID 76: COMPLETED 상태
        RestAssured.given().log().all()
                .queryParam("name", "과거게스트")
                .when().patch("/reservations/76/cancel")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("[예약] 다른 사용자의 예약을 취소하면 403을 반환한다.")
    void 다른_사용자의_예약_취소시_403() {
        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().patch("/reservations/1/cancel")
                .then().log().all()
                .statusCode(403);
    }

    // ── 예약 규칙: 존재하지 않는 시간 ID로 변경(404) ──────────────────────────────

    @Test
    @DisplayName("[예약] 존재하지 않는 테마 슬롯 ID로 예약 변경하면 404를 반환한다.")
    void 존재하지_않는_테마슬롯ID로_예약_변경시_404() {
        Map<String, Object> body = Map.of(
                "themeSlotId", 99999
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
    @DisplayName("[예약] 같은 사용자가 같은 날짜·시간·테마에 이미 예약이 있으면 409를 반환한다.")
    void 같은_사용자가_중복_예약_등록시_409() {
        // theme_slot_id=34 는 미래 날짜에 게스트가 이미 예약함
        Map<String, Object> body = Map.of(
                "name", "게스트",
                "themeSlotId", 34
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }

    // ── 대기 규칙: 대기 신청/취소/조회 ───────────────────────────────────────────

    @Test
    @DisplayName("[대기] 예약된 슬롯에 새 사용자가 대기를 신청하면 대기 예약으로 생성된다.")
    void 예약된_슬롯에_예약_등록시_대기_예약으로_생성된다() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("포비");
        Map<String, Object> body = Map.of("themeSlotId", themeSlotId);

        RestAssured.given().log().all()
                .header("X-Member-Name", "new-waiting")
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo("new-waiting"))
                .body("status", equalTo("1번째 예약대기"));
    }

    @Test
    @DisplayName("[대기] 같은 사용자가 같은 슬롯에 중복 대기하면 409를 반환한다.")
    void 같은_사용자가_중복_대기_등록시_409() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("포비");
        insertWaiting("brown", themeSlotId);
        Map<String, Object> body = Map.of("themeSlotId", themeSlotId);

        RestAssured.given().log().all()
                .header("X-Member-Name", "brown")
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("[대기] 대기 예약을 취소하면 내 예약 조회에서 대기 목록이 사라진다.")
    void 대기_예약_취소시_내_예약_조회에서_취소_예약으로_조회된다() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("포비");
        Map<String, Object> body = Map.of("themeSlotId", themeSlotId);

        Number waitingId = RestAssured.given().log().all()
                .header("X-Member-Name", "cancel-waiting")
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("status", equalTo("1번째 예약대기"))
                .extract()
                .path("id");

        RestAssured.given().log().all()
                .header("X-Member-Name", "cancel-waiting")
                .when().delete("/waitings/{waitingId}", waitingId.longValue())
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header("X-Member-Name", "cancel-waiting")
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("waitingReservationResponses", empty());
    }

    @Test
    @DisplayName("[내 예약] 대기 예약은 대기 순번과 함께 조회된다.")
    void 내_예약_조회시_대기_예약은_대기_순번과_함께_조회된다() {
        RestAssured.given().log().all()
                .queryParam("name", "브라운")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("waitingReservationResponses.status", everyItem(equalTo("1번째 예약대기")))
                .body("waitingReservationResponses.waitingOrder", containsInAnyOrder(1, 1));
    }

    @Test
    @DisplayName("[내 예약] 인증된 사용자 이름 헤더로 내 예약을 조회한다.")
    void 인증된_사용자_이름_헤더로_내_예약을_조회한다() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("confirmed-user");
        insertWaiting("brown", themeSlotId);

        RestAssured.given().log().all()
                .header("X-Member-Name", "brown")
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("waitingReservationResponses.status", everyItem(equalTo("1번째 예약대기")))
                .body("waitingReservationResponses[0].waitingOrder", equalTo(1));
    }

    @Test
    @DisplayName("[자동승격] 확정 예약을 취소하면 첫 번째 대기가 확정되고 다음 대기 순번이 1로 재정렬된다.")
    void 확정_예약_취소시_첫번째_대기가_확정되고_남은_대기_순번이_재정렬된다() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("확정자");
        long confirmedReservationId = findReservationId("확정자", themeSlotId);
        insertWaiting("첫대기", themeSlotId);
        insertWaiting("둘대기", themeSlotId);

        RestAssured.given().log().all()
                .queryParam("name", "확정자")
                .when().patch("/reservations/{reservationId}/cancel", confirmedReservationId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("name", "첫대기")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservationResponses", hasSize(1))
                .body("reservationResponses[0].status", equalTo("CONFIRMED"))
                .body("waitingReservationResponses", empty());

        RestAssured.given().log().all()
                .queryParam("name", "둘대기")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("waitingReservationResponses", hasSize(1))
                .body("waitingReservationResponses[0].status", equalTo("1번째 예약대기"))
                .body("waitingReservationResponses[0].waitingOrder", equalTo(1));
    }

    @Test
    @DisplayName("[대기] 앞 순번 대기를 취소하면 뒤 순번 대기가 1번으로 재정렬된다.")
    void 앞순번_대기_취소시_뒤순번_대기가_첫번째로_재정렬된다() {
        long themeSlotId = createReservedThemeSlotWithConfirmedReservation("확정자");
        long firstWaitingId = insertWaitingAndReturnId("first-waiting", themeSlotId);
        insertWaiting("second-waiting", themeSlotId);

        RestAssured.given().log().all()
                .header("X-Member-Name", "first-waiting")
                .when().delete("/waitings/{waitingId}", firstWaitingId)
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .queryParam("name", "second-waiting")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("waitingReservationResponses", hasSize(1))
                .body("waitingReservationResponses[0].waitingOrder", equalTo(1));
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

    private long createReservedThemeSlotWithConfirmedReservation(String name) {
        long themeSlotId = insertThemeSlot(LocalDate.now().plusDays(30));
        insertReservation(name, "CONFIRMED", themeSlotId);
        return themeSlotId;
    }

    private long insertThemeSlot(LocalDate date) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO theme_slot (theme_id, date, time_id, is_reserved)
                    VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, 4L);
            ps.setObject(2, date);
            ps.setLong(3, 6L);
            ps.setBoolean(4, true);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private void insertReservation(String name, String status, long themeSlotId) {
        jdbcTemplate.update("""
                        INSERT INTO reservation (name, status, theme_slot_id)
                        VALUES (?, ?, ?)
                        """,
                name,
                status,
                themeSlotId
        );
    }

    private void insertWaiting(String name, long themeSlotId) {
        insertWaitingAndReturnId(name, themeSlotId);
    }

    private long insertWaitingAndReturnId(String name, long themeSlotId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO waiting (member_name, date, time_id, theme_id)
                    SELECT ?, date, time_id, theme_id
                    FROM theme_slot
                    WHERE id = ?
                    """, new String[]{"id"});
            ps.setString(1, name);
            ps.setLong(2, themeSlotId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long findReservationId(String name, long themeSlotId) {
        return jdbcTemplate.queryForObject("""
                        SELECT id
                        FROM reservation
                        WHERE name = ?
                        AND theme_slot_id = ?
                        """,
                Long.class,
                name,
                themeSlotId
        );
    }
}
