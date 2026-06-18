package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.service.WaitingPromotionService;
import roomescape.reservation.service.dto.response.ReservationOptionResponse;
import roomescape.reservation.service.dto.response.ReservationsAndWaitingsResponse;

@Sql("/clear.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    WaitingPromotionService waitingPromotionService;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void resetSpy() {
        Mockito.reset(waitingPromotionService);
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(
                    LocalDate.of(2026, 5, 1)
                            .atStartOfDay(ZoneId.of("Asia/Seoul"))
                            .toInstant(),
                    ZoneId.of("Asia/Seoul")
            );
        }
    }

    @Test
    @Sql(scripts = {
            "/clear.sql",
            "/popular-themes-test-data.sql"
    })
    void 전체_날짜와_테마_조회() {
        ReservationOptionResponse responses = RestAssured.given().log().all()
                .when().get("/api/reservations/date-and-theme")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getObject(".", ReservationOptionResponse.class);

        // 기간 검증
        final LocalDate expectedStartDate = LocalDate.of(2026, 05, 01);
        final LocalDate expectedEndDate = expectedStartDate.plusDays(14 - 1);

        final List<LocalDate> actualDates = responses.dates();
        assertThat(actualDates).hasSize(14);

        assertThat(actualDates).doesNotContainAnyElementsOf(
                List.of(
                        expectedStartDate.minusDays(1),
                        expectedEndDate.plusDays(1)
                )
        );

        // 테마 검증
        assertThat(responses.themes()).hasSize(12);
    }

    @Test
    void 날짜와_테마를_선택해_예약가능한_시간_조회() {
        // 시간 추가
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertReservationTime("12:00");
        insertReservationTime("13:00");
        insertReservationTime("14:00");

        // 테마 추가
        insertTheme("링", "공포 테마", "http:~");

        // 예약 전
        List<ReservationTimesWithStatus> timeStatusesBeforeReservation = getReservationTimeStatusResponses();
        assertThat(timeStatusesBeforeReservation.size()).isEqualTo(5);
        assertThat(countReservableTimes(timeStatusesBeforeReservation)).isEqualTo(5);

        // 예약 추가 1
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-05-05", "1", "1");

        // 예약 후
        List<ReservationTimesWithStatus> timeStatusesAfterReservation = getReservationTimeStatusResponses();
        assertThat(timeStatusesAfterReservation.size()).isEqualTo(5);
        assertThat(countReservableTimes(timeStatusesAfterReservation)).isEqualTo(4);
    }

    @Test
    void 예약_가능한_시간_조회시_날짜_형식이_잘못되면_400을_응답한다() {
        RestAssured.given().log().all()
                .when().get("/api/reservations/available-times?date=invalid-date&themeId=1")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("잘못된 요청입니다."));
    }

    @Test
    void 예약_가능한_시간_조회시_테마_id가_없으면_400을_응답한다() {
        RestAssured.given().log().all()
                .when().get("/api/reservations/available-times?date=2026-05-05")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("잘못된 요청입니다."));
    }

    @Nested
    @DisplayName("예약자 이름으로 예약 및 대기 목록을 조회한다")
    class FindReservationsAndWaitingsByCustomerName {

        @Test
        void 예약자_이름으로_예약_및_대기_목록을_조회한다() {
            // given
            final String customerName = "코로구";

            insertReservationTime("10:00");
            insertReservationTime("11:00");

            insertTheme("링", "공포 테마", "http:~");

            insertReservation(customerName, "2026-05-13", 1L, 1L);
            insertReservation("재키", "2026-05-13", 2L, 1L);

            insertWaiting(customerName, "2026-05-13", 2L, 1L);

            // when
            final Response response = RestAssured
                .given().log().all()
                .queryParam("customer-name", customerName)
                .when().get("/api/reservations");

            // then
            final ReservationsAndWaitingsResponse body = response
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getObject(".", ReservationsAndWaitingsResponse.class);

            assertThat(body.reservations()).hasSize(1);
            assertThat(body.reservations().getFirst().name()).isEqualTo(customerName);
            assertThat(body.waitings()).hasSize(1);
            assertThat(body.waitings().getFirst().customerName()).isEqualTo(customerName);
            assertThat(body.waitings().getFirst().rank()).isEqualTo(1);
        }
    }

    @Test
    void 예약을_추가하고_삭제한다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "date", "2026-08-05",
                        "timeId", 1,
                        "themeId", 1
                ))
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .when().delete("/api/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    void 예약_일정을_수정한다() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-08-05", "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-08-06",
                        "timeId", 2
                ))
                .when().put("/api/reservations/1")
                .then().log().all()
                .statusCode(200)
                .body("id", org.hamcrest.Matchers.is(1))
                .body("name", org.hamcrest.Matchers.is("브라운"))
                .body("date", org.hamcrest.Matchers.is("2026-08-06"))
                .body("time.id", org.hamcrest.Matchers.is(2))
                .body("theme.id", org.hamcrest.Matchers.is(1));

        Map<String, Object> updatedReservation = jdbcTemplate.queryForMap(
                "SELECT date, time_id FROM reservation WHERE id = ?",
                1L
        );
        assertThat(updatedReservation.get("DATE").toString()).isEqualTo("2026-08-06");
        assertThat(updatedReservation.get("TIME_ID")).isEqualTo(2L);
    }

    @Test
    void 존재하지_않는_예약을_수정하면_404를_응답한다() {
        insertReservationTime("10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-08-06",
                        "timeId", 1
                ))
                .when().put("/api/reservations/999")
                .then().log().all()
                .statusCode(404)
                .body("message", org.hamcrest.Matchers.is("존재하지 않는 예약입니다."));
    }

    @Test
    void 존재하지_않는_예약_시간으로_수정하면_404를_응답한다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-08-05", "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-08-06",
                        "timeId", 999
                ))
                .when().put("/api/reservations/1")
                .then().log().all()
                .statusCode(404)
                .body("message", org.hamcrest.Matchers.is("존재하지 않는 예약 시간입니다."));
    }

    @Test
    void 예약_수정시_예약일을_입력하지_않으면_400을_응답한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "timeId", 1
                ))
                .when().put("/api/reservations/1")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("예약일을 입력해야 합니다."));
    }

    @Test
    void 과거_시간으로_예약을_수정하면_400을_응답한다() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-08-05", "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-04-30",
                        "timeId", 2
                ))
                .when().put("/api/reservations/1")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("과거 시간으로는 예약할 수 없습니다."));
    }

    @Test
    void 이미_예약된_시간으로_수정하면_409를_응답한다() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-08-05", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "재키", "2026-08-05", "2", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-08-05",
                        "timeId", 2
                ))
                .when().put("/api/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("message", org.hamcrest.Matchers.is("이미 예약이 있는 슬롯입니다."));
    }

    @Test
    void 예약일_당일에는_예약_시작_전이어도_사용자가_예약을_수정할_수_없다() {
        insertReservationTime("10:00");
        insertReservationTime("11:00");
        insertTheme("링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-05-01", "1", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-05-02",
                        "timeId", 2
                ))
                .when().put("/api/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("message", org.hamcrest.Matchers.is("당일 예약은 변경할 수 없습니다."));
    }

    @Test
    void 예약일_당일에는_예약_시작_전이어도_사용자가_예약을_취소할_수_없다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-05-01", "1", "1");

        RestAssured.given().log().all()
                .when().delete("/api/reservations/1")
                .then().log().all()
                .statusCode(409)
                .body("message", org.hamcrest.Matchers.is("당일 예약은 취소할 수 없습니다."));
    }

    @Test
    void 존재하지_않는_예약_시간으로_예약하면_404를_응답한다() {
        insertTheme("링", "공포 테마", "http:~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "date", "2026-05-05",
                        "timeId", 999,
                        "themeId", 1
                ))
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약_시간을_선택하지_않으면_400을_응답한다() {
        insertTheme("링", "공포 테마", "http:~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "date", "2026-05-05",
                        "themeId", 1
                ))
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("예약 시간을 선택해야 합니다."));
    }

    @Test
    void 예약_요청_본문이_null이면_400을_응답한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("null")
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.is("잘못된 요청입니다."));
    }

    @Test
    void 존재하지_않는_테마로_예약하면_404를_응답한다() {
        insertReservationTime("10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "date", "2026-05-05",
                        "timeId", 1,
                        "themeId", 999
                ))
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약자_이름이_비어있으면_400을_응답한다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "",
                        "date", "2026-05-05",
                        "timeId", 1,
                        "themeId", 1
                ))
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Nested
    @DisplayName("예약을 취소하면 대기를 예약으로 승격한다")
    class CancelReservationAndPromoteWaiting {

        @Test
        void 예약_취소_시_대기가_있으면_첫_번째_대기가_예약으로_승격된다() {
            // given
            insertReservationTime("12:00");
            insertTheme("themeName", "description", "url");
            insertReservation("customer", "2026-08-05", 1L, 1L);
            insertWaiting("코로구", "2026-08-05", 1L, 1L);

            // when
            final Response response = RestAssured.given().log().all()
                .delete("/api/reservations/{id}", 1L);

            // then
            response.then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(findReservationName()).isEqualTo("코로구");
            assertThat(countWaitings()).isZero();
        }

        @Test
        void 승격이_실패해도_예약_취소는_성공한다() {
            // given
            insertReservationTime("12:00");
            insertTheme("themeName", "description", "url");
            insertReservation("customer", "2026-05-20", 1L, 1L);
            insertWaiting("수달", "2026-05-20", 1L, 1L);

            doThrow(RuntimeException.class)
                .when(waitingPromotionService).promoteBySlot(any(), anyLong(), anyLong());

            // when
            final Response response = RestAssured.given().log().all()
                .delete("/api/reservations/{id}", 1L);

            // then
            response.then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(countReservations()).isZero();
            assertThat(countWaitings()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("예약 슬롯을 변경하면 기존 슬롯의 대기를 예약으로 승격한다")
    class UpdateReservationAndPromoteWaiting {

        @Test
        void 예약_슬롯_변경_시_대기가_있으면_첫_번째_대기가_예약으로_승격된다() {
            // given
            final String oldSlotDate = "2026-08-05";
            insertReservationTime("12:00");
            insertTheme("themeName", "description", "url");
            insertReservation("customer", oldSlotDate, 1L, 1L);
            insertWaiting("수달", oldSlotDate, 1L, 1L);

            // when
            final Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", "2026-08-07",
                    "timeId", 1))
                .put("/api/reservations/{id}", 1L);

            // then
            response.then().log().all()
                .statusCode(HttpStatus.OK.value());

            assertThat(findReservationNameBySlot(oldSlotDate, 1L, 1L)).isEqualTo("수달");
            assertThat(countWaitings()).isZero();
        }

        @Test
        void 승격이_실패해도_예약_변경은_성공한다() {
            // given
            final String oldSlotDate = "2026-08-05";
            insertReservationTime("12:00");
            insertTheme("themeName", "description", "url");
            insertReservation("customer", oldSlotDate, 1L, 1L);
            insertWaiting("수달", oldSlotDate, 1L, 1L);

            doThrow(RuntimeException.class)
                .when(waitingPromotionService).promoteBySlot(any(), anyLong(), anyLong());

            // when
            final String newSlotDate = "2026-08-07";
            final Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "date", newSlotDate,
                    "timeId", 1))
                .put("/api/reservations/{id}", 1L);

            // then
            response.then().log().all()
                .statusCode(HttpStatus.OK.value());

            assertThat(findReservationNameBySlot(newSlotDate, 1L, 1L)).isEqualTo("customer");
            assertThat(countWaitings()).isEqualTo(1);
        }
    }

    private String findReservationName() {
        return jdbcTemplate.queryForObject("SELECT name FROM reservation", String.class);
    }

    private String findReservationNameBySlot(final String date, final long timeId, final long themeId) {
        return jdbcTemplate.queryForObject("""
                SELECT name FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?
                """,
            String.class,
            date,
            timeId,
            themeId
        );
    }

    private int countReservations() {
        return jdbcTemplate.queryForObject("SELECT count(1) FROM reservation", Integer.class);
    }

    private int countWaitings() {
        return jdbcTemplate.queryForObject("SELECT count(1) FROM waiting", Integer.class);
    }

    private void insertWaiting(final String customerName, final String date, final long timeId, final long themeId) {
        jdbcTemplate.update("""
                INSERT INTO waiting (customer_name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)
                """,
            customerName,
            date,
            timeId,
            themeId
        );
    }

    private void insertReservation(final String name, final String date, final long timeId, final long themeId) {
        jdbcTemplate.update("""
                INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)
                """,
            name,
            date,
            timeId,
            themeId
        );
    }

    private void insertTheme(final String name, final String description, final String url) {
        jdbcTemplate.update("""
                INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)
                """,
            name,
            description,
            url
        );
    }

    private void insertReservationTime(final String startAt) {
        jdbcTemplate.update("""
                INSERT INTO reservation_time (start_at) VALUES (?)
                """,
            startAt
        );
    }

    private static List<ReservationTimesWithStatus> getReservationTimeStatusResponses() {
        return RestAssured.given().log().all()
            .when().get("/api/reservations/available-times?date=2026-05-05&themeId=1")
            .then().log().all()
            .statusCode(200).extract()
            .jsonPath().getList(".", ReservationTimesWithStatus.class);
    }

    private static int countReservableTimes(final List<ReservationTimesWithStatus> timeStatuses) {
        int count = 0;
        for (final ReservationTimesWithStatus timeStatus : timeStatuses) {
            if (!timeStatus.reserved()) {
                count++;
            }
        }
        return count;
    }
}
