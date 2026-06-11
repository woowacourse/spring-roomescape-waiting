package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.domain.reservation.RankedReservation;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.Status;
import roomescape.service.ReservationService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RoomescapeApplicationTest {
    private static final String AVAILABLE_DATE = "2099-06-01";
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationService reservationService;

    @LocalServerPort
    int port;

    @BeforeEach
    void init() {
        RestAssured.port = port;
        jdbcTemplate.update("insert into reservation_time(start_at) values ('10:00')");
        jdbcTemplate.update(
                "insert into theme(name, description, thumbnail_url) values ('공포', '무서워요', 'https://zeze.com')");
        jdbcTemplate.update(
                "insert into theme(name, description, thumbnail_url) values ('개그', '재밌어요', 'https://zeze.com')");
    }

    @Test
    void 예약_생성_후_사용_시간_조회시_해당_시간이_제외된다() {
        int before = availableCount(AVAILABLE_DATE, 1);

        reserve("제제", AVAILABLE_DATE, 1L, 1L, 201);

        int after = availableCount(AVAILABLE_DATE, 1);
        assertThat(after).isEqualTo(before - 1);
    }

    @Test
    void 예약_없는_날짜_조회시_전체_시간이_반환된다() {
        int total = RestAssured.given()
                .when().get("/times")
                .then().statusCode(200)
                .extract().jsonPath().getList(".").size();

        int available = availableCount(AVAILABLE_DATE, 1);

        assertThat(available).isEqualTo(total);
    }

    @Test
    void 다른_테마_예약은_사용_시간_조회에_영향을_주지_않는다() {
        int before = availableCount(AVAILABLE_DATE, 1);

        reserve("제제", AVAILABLE_DATE, 1L, 2L, 201);

        int after = availableCount(AVAILABLE_DATE, 1);
        assertThat(after).isEqualTo(before);
    }

    @Test
    void themeId_없이_사용_시간_조회시_400을_반환한다() {
        RestAssured.given()
                .when().get("/times/available?date=" + AVAILABLE_DATE)
                .then().statusCode(400);
    }

    @Test
    void date_없이_가용_시간_조회시_400을_반환한다() {
        RestAssured.given()
                .when().get("/times/available?themeId=1")
                .then().statusCode(400);
    }

    @Test
    void 존재하지_않는_테마_조회시_404를_반환한다() {
        RestAssured.given()
                .when().get("/themes/999")
                .then().statusCode(404);
    }

    @Test
    void 존재하지_않는_예약_조회시_404을_반환한다() {
        RestAssured.given()
                .when().get("/reservations/999")
                .then().statusCode(404);
    }

    @Test
    void 중복_예약_수행시_409를_반환한다() {
        String name = "zeze";
        String date = "2099-05-14";
        long timeId = 1L;
        long themeId = 1L;
        reserve(name, date, timeId, themeId, 201);
        reserve(name, date, timeId, themeId, 409);
    }

    @Test
    void 예약이_존재하는_시간을_지우면_409를_반환한다() {
        String name = "zeze";
        String date = "2099-05-14";
        long timeId = 1L;
        long themeId = 1L;
        reserve(name, date, timeId, themeId, 201);

        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all().statusCode(409);
    }

    @Test
    void 예약_가능_날짜_조회시_기준_날짜를_과거로_설정하면_422를_반환한다() {
        String past = "2020-01-01";

        RestAssured.given()
                .when().get("/times/available?date=" + past + "&themeId=1")
                .then().statusCode(422);
    }

    @Test
    void 과거_예약_생성시_422를_반환한다() {
        String past = "2020-01-01";

        reserve("zeze", past, 1L, 1L, 422);
    }

    @Test
    void 이름으로_조회시_정상적으로_반환한다() {
        reserve("zeze", "2099-05-01", 1L, 1L, 201);
        reserve("zeze", "2099-05-02", 1L, 1L, 201);
        reserve("zeze", "2099-05-03", 1L, 1L, 201);
        reserve("mingu", "2099-05-04", 1L, 1L, 201);

        RestAssured.given().params("name", "zeze")
                .when().get("/reservations")
                .then().log().all()
                .body("size()", is(3));
    }

    private int availableCount(String date, long themeId) {
        return RestAssured.given()
                .when().get("/times/available?date=" + date + "&themeId=" + themeId)
                .then().statusCode(200)
                .extract().jsonPath().getList(".").size();
    }

    @Test
    void 예약_생성_후_단건_조회가_된다() {
        int id = reserveAndGetId("zeze", "2099-06-01", 1L, 1L);

        RestAssured.given()
                .when().get("/reservations/" + id)
                .then().statusCode(200)
                .body("name", org.hamcrest.Matchers.equalTo("zeze"))
                .body("id", org.hamcrest.Matchers.equalTo(id));
    }

    @Test
    void 예약_생성_후_전체_목록에서_조회된다() {
        reserve("zeze", "2099-06-01", 1L, 1L, 201);
        reserve("mingu", "2099-06-02", 1L, 1L, 201);

        RestAssured.given()
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void 첫번째_예약은_승인_상태이다() {
        int id = reserveAndGetId("zeze", "2099-06-01", 1L, 1L);

        RestAssured.given()
                .when().get("/reservations/" + id)
                .then().statusCode(200)
                .body("state", org.hamcrest.Matchers.equalTo("승인"))
                .body("rank", org.hamcrest.Matchers.equalTo(0));
    }

    @Test
    void 같은_슬롯에_두번째_예약은_대기_상태이다() {
        String date = "2099-06-10";
        reserveAndGetId("zeze", date, 1L, 1L);
        int waitingId = reserveAndGetId("mingu", date, 1L, 1L);

        RestAssured.given()
                .when().get("/reservations/" + waitingId)
                .then().statusCode(200)
                .body("state", org.hamcrest.Matchers.equalTo("대기"))
                .body("rank", org.hamcrest.Matchers.equalTo(1));
    }

    @Test
    void 예약_수정_성공한다() {
        int id = reserveAndGetId("zeze", "2099-06-01", 1L, 1L);

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("name", "zeze");
        updateParams.put("date", "2099-07-01");
        updateParams.put("timeId", 1L);
        updateParams.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateParams)
                .when().put("/reservations/" + id)
                .then().statusCode(200)
                .body("date", org.hamcrest.Matchers.equalTo("2099-07-01"));
    }

    @Test
    void 예약_삭제_성공한다() {
        int id = reserveAndGetId("zeze", "2099-06-01", 1L, 1L);

        RestAssured.given()
                .when().delete("/reservations/" + id)
                .then().statusCode(200);

        RestAssured.given()
                .when().get("/reservations/" + id)
                .then().statusCode(404);
    }

    @Test
    void 예약_생성시_이름이_없으면_400을_반환한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2099-06-01");
        params.put("timeId", 1L);
        params.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(400);
    }

    @Test
    void 예약_생성시_timeId가_없으면_400을_반환한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "zeze");
        params.put("date", "2099-06-01");
        params.put("themeId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(400);
    }

    @Test
    void 존재하지_않는_시간으로_예약시_404를_반환한다() {
        reserve("zeze", "2099-06-01", 999L, 1L, 404);
    }

    @Test
    void 존재하지_않는_테마로_예약시_404를_반환한다() {
        reserve("zeze", "2099-06-01", 1L, 999L, 404);
    }


    @Test
    void 동시에_10명이_첫_예약_요청시_1명만_승인상태가_된다() throws Exception {
        // 한 슬롯에 Approve된 예약은 반드시 1건 이하여야 한다.
        int threads = 10;
        var ready = new CountDownLatch(threads);
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(threads);
        var approved = new AtomicInteger();
        var waiting = new AtomicInteger();

        var pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            ReservationCreateRequest request = RoomEscapeFixture.reservationCreateRequestWithName(
                    new ReservationName(i + ""));
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    RankedReservation result = reservationService.reserve(request, LocalDateTime.now());

                    if (result.getReservation().getStatus() == Status.APPROVED) {
                        approved.incrementAndGet();
                    }
                    if (result.getReservation().getStatus() == Status.WAITING) {
                        waiting.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        done.await();

        AssertionsForClassTypes.assertThat(approved.get()).isEqualTo(1);
        AssertionsForClassTypes.assertThat(waiting.get()).isEqualTo(9);
    }

    private int reserveAndGetId(String name, String date, Long timeId, Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getInt("id");
    }

    private void reserve(String name, String date, Long timeId, Long themeId, int expectedStatusCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("date", date);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().statusCode(expectedStatusCode);
    }
}