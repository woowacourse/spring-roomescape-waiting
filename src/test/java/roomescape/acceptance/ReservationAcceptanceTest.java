package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;
import roomescape.fixture.Scenario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationAcceptanceTest {

    private static final String AUTHORIZATION = "Authorization";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("GET /reservations/mine - 로그인 사용자의 예약만 조회한다")
    void getMyReservationsReturnsOnlyLoginUserReservations() {
        Scenario.reservation(jdbcTemplate).member("브라운").date(Fixtures.daysFromNow(-6).toString()).save();
        Scenario.ExistingReservation mine = Scenario.reservation(jdbcTemplate).member("브라운").date(Fixtures.daysFromNow(-5).toString()).save();
        Scenario.reservation(jdbcTemplate).member("다른사람").date(Fixtures.daysFromNow(-4).toString()).save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, mine.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2))
                .body("reservations.name", org.hamcrest.Matchers.everyItem(equalTo("브라운")));
    }

    @Test
    @DisplayName("GET /reservations/mine - 예약 확정과 예약 대기를 상태별로 함께 조회한다")
    void getMyReservationsReturnsReservedAndWaitingByStatus() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .date("9999-12-31").theme("공포A").time("10:00").store("강남점")
                .member("브라운").save();
        Scenario.ExistingReservation existing = Scenario.reservation(jdbcTemplate)
                .date("9999-12-31").theme("공포B").time("11:00").store("강남점")
                .member("샤를").save();
        Scenario.waitingReservation(jdbcTemplate)
                .date("9999-12-31").onTheme(existing.themeId()).onTime(existing.timeId()).onStore(existing.storeId())
                .member("아론").save();
        Scenario.waitingReservation(jdbcTemplate)
                .date("9999-12-31").onTheme(existing.themeId()).onTime(existing.timeId()).onStore(existing.storeId())
                .member("브라운").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", equalTo("브라운"))
                .body("reservations[0].themeName", equalTo("공포A"))
                .body("waitingReservations.size()", is(1))
                .body("waitingReservations[0].name", equalTo("브라운"))
                .body("waitingReservations[0].themeName", equalTo("공포B"))
                .body("waitingReservations[0].waitingOrder", equalTo(2))
                .body("hasNext", equalTo(false));
    }

    @Test
    @DisplayName("GET /reservations/mine - 앞 대기자가 취소되면 재조회 시 대기 순번이 줄어든다")
    void getMyReservationsWaitingOrderDecreasesWhenEarlierWaiterCancels() {
        // 주인공 - 브라운
        Scenario.ExistingReservation existing = Scenario.reservation(jdbcTemplate).date("9999-12-31").theme("공포")
                .time("10:00").store("강남점").member("샤를").save();
        Long themeId = existing.themeId();
        Long timeId = existing.timeId();
        Long storeId = existing.storeId();

        Scenario.ExistingReservation waitingReservation1 = Scenario.waitingReservation(jdbcTemplate)
                .date("9999-12-31").onTheme(themeId).onTime(timeId).onStore(storeId)
                .member("아론").save();
        Scenario.ExistingReservation waitingReservation2 = Scenario.waitingReservation(jdbcTemplate)
                .date("9999-12-31").onTheme(themeId).onTime(timeId).onStore(storeId)
                .member("재키").save();
        Scenario.ExistingReservation mine = Scenario.waitingReservation(jdbcTemplate)
                .date("9999-12-31").onTheme(themeId).onTime(timeId).onStore(storeId)
                .member("브라운").save();

        //1차 조회 - 대기 3번
        RestAssured.given().log().all()
                .header(AUTHORIZATION, mine.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("waitingReservations[0].waitingOrder", equalTo(3));

        //재키 삭제
        RestAssured.given().log().all()
                .header(AUTHORIZATION, waitingReservation2.bearer())
                .when().post("/reservations/" + waitingReservation2.reservationId() + "/cancel")
                .then().log().all()
                .statusCode(200);

        //재조회 - 대기 2번
        RestAssured.given().log().all()
                .header(AUTHORIZATION, mine.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("waitingReservations[0].waitingOrder", equalTo(2));
    }

    @Test
    @DisplayName("GET /reservations/mine - 다른 슬롯의 대기자는 내 대기 순번에 영향을 주지 않는다")
    void getMyReservationsWaitingOrderIgnoresOtherSlotWaiters() {
        Scenario.ExistingReservation targetSlot = Scenario.reservation(jdbcTemplate)
                .date("9999-12-31").theme("공포").time("10:00").store("강남점")
                .member("샤를").save();
        Scenario.ExistingReservation otherSlot = Scenario.reservation(jdbcTemplate)
                .date("9999-12-31").theme("공포").time("11:00").store("강남점")
                .member("재키").save();

        Scenario.ExistingReservation otherSlotWaiting = Scenario.waitingReservation(jdbcTemplate)
                .date("9999-12-31").onTheme(otherSlot.themeId()).onTime(otherSlot.timeId())
                .onStore(otherSlot.storeId())
                .member("아론").save();
        Scenario.ExistingReservation targetSlotFirstWaiting = Scenario.waitingReservation(jdbcTemplate)
                .date("9999-12-31").onTheme(targetSlot.themeId()).onTime(targetSlot.timeId())
                .onStore(targetSlot.storeId())
                .member("재키").save();
        Scenario.ExistingReservation mine = Scenario.waitingReservation(jdbcTemplate)
                .date("9999-12-31").onTheme(targetSlot.themeId()).onTime(targetSlot.timeId())
                .onStore(targetSlot.storeId())
                .member("브라운").save();

        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 09:00:00",
                otherSlotWaiting.reservationId());
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 10:00:00",
                targetSlotFirstWaiting.reservationId());
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 11:00:00",
                mine.reservationId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, mine.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("waitingReservations.size()", is(1))
                .body("waitingReservations[0].name", equalTo("브라운"))
                .body("waitingReservations[0].time", equalTo("10:00:00"))
                .body("waitingReservations[0].waitingOrder", equalTo(2));
    }

    @Test
    @DisplayName("GET /reservations/mine - 토큰이 없으면 401과 메시지를 반환한다")
    void getMyReservationsReturns401WithoutToken() {
        RestAssured.given().log().all()
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("GET /reservations/{id} - 단건을 조회한다")
    void getReservationReturnsSingle() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .when().get("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(200)
                .body("name", equalTo("브라운"));
    }

    @Test
    @DisplayName("POST /reservations - 예약을 생성한다")
    void createReservation() {
        Scenario.BookableSlot slot = Scenario.bookableSlot(jdbcTemplate, "브라운");
        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(1).toString(),
                "themeId", slot.themeId(),
                "timeId", slot.timeId(),
                "storeId", slot.storeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, slot.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .header("Location", matchesPattern("/reservations/\\d+"));
    }

    @Test
    @DisplayName("POST /reservations - 토큰이 없으면 401과 메시지를 반환한다")
    void createReservationReturns401WithoutToken() {
        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(1).toString(),
                "themeId", 1,
                "timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("POST /reservations - 같은 날짜/시간/테마 중복이면 409과 메시지를 반환한다")
    void createReservationReturns409OnDuplicateSlot() {
        Scenario.ExistingReservation existing = Scenario.reservation(jdbcTemplate).member("기존").date(Fixtures.daysFromNow(1).toString())
                .save();
        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(1).toString(),
                "themeId", existing.themeId(),
                "timeId", existing.timeId(),
                "storeId", existing.storeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("code", equalTo("DUPLICATE_RESERVATION"));
    }

    @Test
    @DisplayName("POST /reservations - 과거 날짜 시간이면 422과 메시지를 반환한다")
    void createReservationReturns422OnPastDateTime() {
        Scenario.BookableSlot slot = Scenario.bookableSlot(jdbcTemplate, "브라운");
        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(-1).toString(),
                "themeId", slot.themeId(),
                "timeId", slot.timeId(),
                "storeId", slot.storeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, slot.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422)
                .body("code", equalTo("PAST_DATE_TIME_RESERVATION"));
    }

    @Test
    @DisplayName("POST /reservations - 본문의 date가 형식 오류면 400과 메시지를 반환한다")
    void createReservationReturns400OnInvalidDateFormat() {
        String body = """
                {"date":"abc","themeId":1,"timeId":1}
                """;

        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("POST /reservations - 본문 JSON 문법 오류면 400과 메시지를 반환한다")
    void createReservationReturns400OnMalformedJson() {
        String brokenBody = "{\"themeId\":1";

        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .contentType(ContentType.JSON)
                .body(brokenBody)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("GET /reservations/{id} - 없는 id면 404과 메시지를 반환한다")
    void getReservationReturns404WhenIdDoesNotExist() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .when().get("/reservations/9999")
                .then().log().all()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /reservations/{id} - 본인의 예약을 변경한다")
    void updateOwnReservation() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").date(Fixtures.daysFromNow(25).toString())
                .save();
        long newThemeId = DbFixtures.insertTheme(jdbcTemplate, "테마2");
        long newTimeId = DbFixtures.insertTime(jdbcTemplate, "11:00");
        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(26).toString(),
                "themeId", newThemeId,
                "timeId", newTimeId);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(200)
                .body("date", equalTo(Fixtures.daysFromNow(26).toString()));
    }

    @Test
    @DisplayName("PUT /reservations/{id} - 소유자 불일치면 403과 메시지를 반환한다")
    void updateReservationReturns403OnOwnerMismatch() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").date(Fixtures.daysFromNow(25).toString())
                .save();
        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(26).toString(),
                "themeId", reserved.themeId(),
                "timeId", reserved.timeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "다른사람"))
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(403)
                .body("code", equalTo("RESERVATION_OWNER_MISMATCH"));
    }

    @Test
    @DisplayName("PUT /reservations/{id} - 과거 예약을 변경하려 하면 422과 메시지를 반환한다")
    void updateReservationReturns422WhenModifyingPastReservation() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").date(Fixtures.daysFromNow(-6).toString())
                .save();
        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(26).toString(),
                "themeId", reserved.themeId(),
                "timeId", reserved.timeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(422)
                .body("code", equalTo("PAST_RESERVATION_MODIFICATION"));
    }

    @Test
    @DisplayName("PUT /reservations/{id} - 새 일정이 과거이면 422과 메시지를 반환한다")
    void updateReservationReturns422WhenNewScheduleIsPast() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").date(Fixtures.daysFromNow(25).toString())
                .save();
        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(-6).toString(),
                "themeId", reserved.themeId(),
                "timeId", reserved.timeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(422)
                .body("code", equalTo("PAST_DATE_TIME_RESERVATION"));
    }

    @Test
    @DisplayName("PUT /reservations/{id} - 새 일정이 이미 예약된 슬롯이면 409과 메시지를 반환한다")
    void updateReservationReturns409WhenNewSlotIsAlreadyReserved() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").date(Fixtures.daysFromNow(25).toString())
                .save();
        long otherTimeId = DbFixtures.insertTime(jdbcTemplate, "11:00");
        Scenario.reservation(jdbcTemplate)
                .member("다른사람").onTheme(reserved.themeId()).onTime(otherTimeId).onStore(reserved.storeId())
                .date(Fixtures.daysFromNow(26).toString()).save();
        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(26).toString(),
                "themeId", reserved.themeId(),
                "timeId", otherTimeId);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(409)
                .body("code", equalTo("DUPLICATE_RESERVATION"));
    }

    @Test
    @DisplayName("PUT /reservations/{id} - 예약 대기를 수정하려는 경우 409과 메시지를 반환한다")
    void updateReservationReturns409WhenModifyingWaiting() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .member("브라운")
                .date(Fixtures.daysFromNow(25).toString())
                .status("WAITING")
                .time("10:00")
                .save();

        long otherTimeId = DbFixtures.insertTime(jdbcTemplate, "11:00");
        Map<String, Object> body = Map.of(
                "date", reserved.date(),
                "themeId", reserved.themeId(),
                "timeId", otherTimeId);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(409)
                .body("code", equalTo("RESERVATION_NOT_RESERVED"));
    }

    @Test
    @DisplayName("POST /reservations/{id}/cancel - 본인의 예약을 취소한다")
    void deleteOwnReservation() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .when().post("/reservations/" + reserved.reservationId() + "/cancel")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /reservations/{id}/cancel - 본인 확정 예약을 취소하면 같은 슬롯의 대기가 승격된다")
    void promotesWaitingWhenOwnReservedIsCanceled() {
        String date = Fixtures.daysFromNow(1).toString();
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .member("예약자").date(date).save();
        Scenario.ExistingReservation waiting = Scenario.waitingReservation(jdbcTemplate)
                .member("대기자").date(date)
                .onTheme(reserved.themeId()).onTime(reserved.timeId()).onStore(reserved.storeId())
                .save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .when().post("/reservations/" + reserved.reservationId() + "/cancel")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, waiting.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", equalTo("대기자"))
                .body("waitingReservations.size()", is(0));
    }

    @Test
    @DisplayName("POST /reservations/{id}/cancel - 소유자 불일치면 403과 메시지를 반환한다")
    void deleteReservationReturns403OnOwnerMismatch() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "다른사람"))
                .when().post("/reservations/" + reserved.reservationId() + "/cancel")
                .then().log().all()
                .statusCode(403)
                .body("code", equalTo("RESERVATION_OWNER_MISMATCH"));
    }

    @Test
    @DisplayName("POST /reservations/{id}/cancel - 없는 id면 404과 메시지를 반환한다")
    void deleteReservationReturns404WhenIdDoesNotExist() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .when().post("/reservations/9999/cancel")
                .then().log().all()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /reservations/{id}/cancel - 토큰이 없으면 401과 메시지를 반환한다")
    void deleteReservationReturns401WithoutToken() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .when().post("/reservations/" + reserved.reservationId() + "/cancel")
                .then().log().all()
                .statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }

    /**
     * createWaitingReservation 1. 정상테스트 2. 과거 예약 대기 생성 시도 3. 예약 확정자가 없는 예약 대기 생성 시도 4. 본인 중복 예약 대기 생성 시도
     */
    @Test
    @DisplayName("POST /reservations/waiting - 예약 대기를 생성한다")
    void createWaitingReservation() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .member("브라운")
                .date(Fixtures.daysFromNow(25).toString())
                .status("RESERVED")
                .time("10:00")
                .save();

        String waitingBearer = DbFixtures.memberBearer(jdbcTemplate, "샤를");

        Map<String, Object> body = Map.of(
                "date", reserved.date(),
                "themeId", reserved.themeId(),
                "timeId", reserved.timeId(),
                "storeId", reserved.storeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, waitingBearer)
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201)
                .header("Location", matchesPattern("/reservations/\\d+"));
    }


    @Test
    @DisplayName("POST /reservations/waiting - 과거 날짜이면 422과 메시지를 반환한다")
    void createWaitingReservationReturns422OnPastDate() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .member("브라운")
                .date(Fixtures.daysFromNow(-3650).toString())
                .status("RESERVED")
                .time("10:00")
                .save();

        String waitingBearer = DbFixtures.memberBearer(jdbcTemplate, "샤를");

        Map<String, Object> body = Map.of(
                "date", reserved.date(),
                "themeId", reserved.themeId(),
                "timeId", reserved.timeId(),
                "storeId", reserved.storeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, waitingBearer)
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(422)
                .body("code", equalTo("PAST_DATE_TIME_RESERVATION"));
    }

    @Test
    @DisplayName("POST /reservations/waiting - 예약 확정자가 없으면 409과 메시지를 반환한다")
    void createWaitingReservationReturns409WhenNoConfirmedReservation() {
        Scenario.BookableSlot slot = Scenario.bookableSlot(jdbcTemplate, "브라운");

        Map<String, Object> body = Map.of(
                "date", Fixtures.daysFromNow(25).toString(),
                "themeId", slot.themeId(),
                "timeId", slot.timeId(),
                "storeId", slot.storeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, slot.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(409)
                .body("code", equalTo("RESERVATION_NOT_FOUND_FOR_WAITING"));
    }

    @Test
    @DisplayName("POST /reservations - 같은 사용자가 중복된 예약 대기가 존재하는 경우 409과 메시지를 반환한다")
    void createWaitingReservationReturns409OnDuplicateWaitingBySameUser() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .member("브라운")
                .date(Fixtures.daysFromNow(25).toString())
                .status("RESERVED")
                .time("10:00")
                .save();
        Scenario.ExistingReservation waiting = Scenario.waitingReservation(jdbcTemplate)
                .member("샤를")
                .date(Fixtures.daysFromNow(25).toString())
                .onTime(reserved.timeId())
                .onTheme(reserved.themeId())
                .onStore(reserved.storeId())
                .save();

        Map<String, Object> body = Map.of(
                "date", waiting.date(),
                "themeId", waiting.themeId(),
                "timeId", waiting.timeId(),
                "storeId", waiting.storeId()
        );

        RestAssured.given().log().all()
                .header(AUTHORIZATION, waiting.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(409)
                .body("code", equalTo("DUPLICATE_WAITING_RESERVATION"));
    }
}
