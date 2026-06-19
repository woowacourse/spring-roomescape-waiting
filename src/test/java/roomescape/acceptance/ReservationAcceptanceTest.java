package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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
    void GET_reservations_mine_로그인_사용자의_예약만_조회한다() {
        Scenario.reservation(jdbcTemplate).member("브라운").date("2026-05-01").save();
        Scenario.ExistingReservation mine = Scenario.reservation(jdbcTemplate).member("브라운").date("2026-05-02").save();
        Scenario.reservation(jdbcTemplate).member("다른사람").date("2026-05-03").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, mine.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2))
                .body("reservations.name", org.hamcrest.Matchers.everyItem(equalTo("브라운")));
    }

    @Test
    void GET_reservations_mine_예약_확정과_예약_대기를_상태별로_함께_조회한다() {
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
    void GET_reservations_mine_앞_대기자가_취소되면_재조회시_대기_순번이_줄어든다() {
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
                .when().delete("/reservations/waiting/" + waitingReservation2.reservationId())
                .then().log().all()
                .statusCode(204);

        //재조회 - 대기 2번
        RestAssured.given().log().all()
                .header(AUTHORIZATION, mine.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("waitingReservations[0].waitingOrder", equalTo(2));
    }

    @Test
    void GET_reservations_mine_다른_슬롯의_대기자는_내_대기_순번에_영향을_주지_않는다() {
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
    void GET_reservations_mine_토큰이_없으면_401과_메시지를_반환한다() {
        RestAssured.given().log().all()
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(401)
                .body("message", equalTo("인증이 필요합니다. 로그인 후 이용해주세요."));
    }

    @Test
    void GET_reservations_id_단건을_조회한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .when().get("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(200)
                .body("name", equalTo("브라운"));
    }

    @Test
    void POST_reservations_예약을_생성한다() {
        Scenario.BookableSlot slot = Scenario.bookableSlot(jdbcTemplate, "브라운");
        Map<String, Object> body = Map.of(
                "date", "2026-05-08",
                "themeId", slot.themeId(),
                "timeId", slot.timeId(),
                "storeId", slot.storeId(),
                "amount", 10_000);

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
    void POST_reservations_토큰이_없으면_401과_메시지를_반환한다() {
        Map<String, Object> body = Map.of(
                "date", "2026-05-08",
                "themeId", 1,
                "timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401)
                .body("message", equalTo("인증이 필요합니다. 로그인 후 이용해주세요."));
    }

    @Test
    void POST_reservations_같은_날짜시간테마_중복이면_409과_메시지를_반환한다() {
        Scenario.ExistingReservation existing = Scenario.reservation(jdbcTemplate).member("기존").date("2026-05-08")
                .save();
        Map<String, Object> body = Map.of(
                "date", "2026-05-08",
                "themeId", existing.themeId(),
                "timeId", existing.timeId(),
                "storeId", DbFixtures.defaultStoreId(jdbcTemplate),
                "amount", 10_000);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("해당 날짜·시간·테마에 이미 예약이 존재합니다. 다른 날짜·시간·테마를 선택해주세요."));
    }

    @Test
    void POST_reservations_과거_날짜_시간이면_422과_메시지를_반환한다() {
        Scenario.BookableSlot slot = Scenario.bookableSlot(jdbcTemplate, "브라운");
        Map<String, Object> body = Map.of(
                "date", "2026-05-06",
                "themeId", slot.themeId(),
                "timeId", slot.timeId(),
                "storeId", slot.storeId(),
                "amount", 10_000);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, slot.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다."));
    }

    @Test
    void POST_reservations_본문의_date가_형식_오류면_400과_메시지를_반환한다() {
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
                .body("message", equalTo("'date' 값 'abc'은(는) yyyy-MM-dd 형식이어야 합니다."));
    }

    @Test
    void POST_reservations_본문_JSON_문법_오류면_400과_메시지를_반환한다() {
        String brokenBody = "{\"themeId\":1";

        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .contentType(ContentType.JSON)
                .body(brokenBody)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("요청 본문 형식이 올바르지 않습니다."));
    }

    @Test
    void GET_reservations_id_없는_id면_404과_메시지를_반환한다() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .when().get("/reservations/9999")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("예약을(를) 찾을 수 없습니다. id=9999"));
    }

    @Test
    void PUT_reservations_id_본인의_예약을_변경한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").date("2026-06-01")
                .save();
        long newThemeId = DbFixtures.insertTheme(jdbcTemplate, "테마2");
        long newTimeId = DbFixtures.insertTime(jdbcTemplate, "11:00");
        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", newThemeId,
                "timeId", newTimeId);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(200)
                .body("date", equalTo("2026-06-02"));
    }

    @Test
    void PUT_reservations_id_소유자_불일치면_403과_메시지를_반환한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").date("2026-06-01")
                .save();
        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", reserved.themeId(),
                "timeId", reserved.timeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "다른사람"))
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(403)
                .body("message", equalTo("본인의 예약만 취소 혹은 변경 가능합니다."));
    }

    @Test
    void PUT_reservations_id_과거_예약을_변경하려_하면_422과_메시지를_반환한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").date("2026-05-01")
                .save();
        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", reserved.themeId(),
                "timeId", reserved.timeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("이미 지난 예약은 수정할 수 없습니다."));
    }

    @Test
    void PUT_reservations_id_새_일정이_과거이면_422과_메시지를_반환한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").date("2026-06-01")
                .save();
        Map<String, Object> body = Map.of(
                "date", "2026-05-01",
                "themeId", reserved.themeId(),
                "timeId", reserved.timeId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(422)
                .body("message", equalTo("예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다."));
    }

    @Test
    void PUT_reservations_id_새_일정이_이미_예약된_슬롯이면_409과_메시지를_반환한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").date("2026-06-01")
                .save();
        long otherTimeId = DbFixtures.insertTime(jdbcTemplate, "11:00");
        Scenario.reservation(jdbcTemplate)
                .member("다른사람").onTheme(reserved.themeId()).onTime(otherTimeId).onStore(reserved.storeId())
                .date("2026-06-02").save();

        Map<String, Object> body = Map.of(
                "date", "2026-06-02",
                "themeId", reserved.themeId(),
                "timeId", otherTimeId);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .contentType(ContentType.JSON)
                .body(body)
                .when().put("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("해당 날짜·시간·테마에 이미 예약이 존재합니다. 다른 날짜·시간·테마를 선택해주세요."));
    }

    @Test
    void PUT_reservations_id_예약_대기를_수정하려는_경우_409과_메시지를_반환한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .member("브라운")
                .date("2026-06-01")
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
                .body("message", equalTo("해당 예약은 예약 확정 상태가 아닙니다. 현재 예약 상태 값: WAITING"));
    }

    @Test
    void DELETE_reservations_id_본인의_예약을_취소한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .when().delete("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void DELETE_reservations_id_첫번째_예약_대기를_예약_확정으로_승격한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .date("9999-12-31").member("브라운").save();
        Scenario.ExistingReservation firstWaiting = Scenario.waitingReservation(jdbcTemplate)
                .date(reserved.date()).onTheme(reserved.themeId()).onTime(reserved.timeId()).onStore(reserved.storeId())
                .member("샤를").save();
        Scenario.ExistingReservation secondWaiting = Scenario.waitingReservation(jdbcTemplate)
                .date(reserved.date()).onTheme(reserved.themeId()).onTime(reserved.timeId()).onStore(reserved.storeId())
                .member("아론").save();

        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 08:00:00",
                firstWaiting.reservationId());
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", "2026-05-01 09:00:00",
                secondWaiting.reservationId());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .when().delete("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, firstWaiting.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].id", equalTo((int) firstWaiting.reservationId()))
                .body("waitingReservations.size()", is(0));

        RestAssured.given().log().all()
                .header(AUTHORIZATION, secondWaiting.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0))
                .body("waitingReservations.size()", is(1))
                .body("waitingReservations[0].id", equalTo((int) secondWaiting.reservationId()))
                .body("waitingReservations[0].waitingOrder", equalTo(1));
    }

    @Test
    void DELETE_reservations_id_예약_대기이면_409과_메시지를_반환한다() {
        Scenario.ExistingReservation waiting = Scenario.waitingReservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, waiting.bearer())
                .when().delete("/reservations/" + waiting.reservationId())
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("해당 예약은 예약 확정 상태가 아닙니다. 현재 예약 상태 값: WAITING"));
    }

    @Test
    void DELETE_reservations_waiting_id_본인의_예약_대기를_취소한다() {
        Scenario.ExistingReservation waiting = Scenario.waitingReservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, waiting.bearer())
                .when().delete("/reservations/waiting/" + waiting.reservationId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void DELETE_reservations_waiting_id_예약_확정이면_409과_메시지를_반환한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, reserved.bearer())
                .when().delete("/reservations/waiting/" + reserved.reservationId())
                .then().log().all()
                .statusCode(409)
                .body("message", equalTo("해당 예약은 예약 대기 상태가 아닙니다. 현재 예약 상태 값: RESERVED"));
    }

    @Test
    void DELETE_reservations_id_소유자_불일치면_403과_메시지를_반환한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "다른사람"))
                .when().delete("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(403)
                .body("message", equalTo("본인의 예약만 취소 혹은 변경 가능합니다."));
    }

    @Test
    void DELETE_reservations_id_없는_id면_404과_메시지를_반환한다() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .when().delete("/reservations/9999")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("예약을(를) 찾을 수 없습니다. id=9999"));
    }

    @Test
    void DELETE_reservations_id_토큰이_없으면_401과_메시지를_반환한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").save();

        RestAssured.given().log().all()
                .when().delete("/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(401)
                .body("message", equalTo("인증이 필요합니다. 로그인 후 이용해주세요."));
    }

    /**
     * createWaitingReservation 1. 정상테스트 2. 과거 예약 대기 생성 시도 3. 예약 확정자가 없는 예약 대기 생성 시도 4. 본인 중복 예약 대기 생성 시도
     */
    @Test
    void POST_reservations_waiting_예약대기를_생성한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .member("브라운")
                .date("2026-06-01")
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
    void POST_reservations_waiting_과거_날짜이면_422과_메시지를_반환한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .member("브라운")
                .date("0000-01-01")
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
                .body("message", equalTo("예약 일정이 유효하지 않습니다. 예약 날짜와 시간은 현시간 이후여야 합니다."));
    }

    @Test
    void POST_reservations_waiting_예약_확정자가_없으면_409과_메시지를_반환한다() {
        Scenario.BookableSlot slot = Scenario.bookableSlot(jdbcTemplate, "브라운");

        Map<String, Object> body = Map.of(
                "date", "2026-06-01",
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
                .body("message", equalTo("확정 예약이 없으므로 대기 예약 생성이 불가능합니다."));
    }

    @Test
    void POST_reservations_같은사용자가_중복된_예약_대기가_존재하는_경우_409과_메세지를_반환한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .member("브라운")
                .date("2026-06-01")
                .status("RESERVED")
                .time("10:00")
                .save();
        Scenario.ExistingReservation waiting = Scenario.waitingReservation(jdbcTemplate)
                .member("샤를")
                .date("2026-06-01")
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
                .body("message", equalTo("이미 해당 슬롯에 예약 대기 중입니다."));
    }
}
