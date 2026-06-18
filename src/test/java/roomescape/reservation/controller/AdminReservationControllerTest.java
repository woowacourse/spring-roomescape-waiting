package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
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
import roomescape.reservation.service.WaitingPromotionService;
import roomescape.reservation.service.dto.response.ReservationResponse;

@Sql("/clear.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminReservationControllerTest {

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
    void 예약_목록이_없으면_빈_배열을_응답한다() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 예약_목록을_조회한다() {
        insertReservationTime("10:00");
        insertTheme("링", "공포 테마", "http:~");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", "2026-08-05", "1", "1");

        List<ReservationResponse> reservations = RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        assertThat(reservations).hasSize(countReservations());
    }

    @Test
    void 생성한_예약을_관리자_목록에서_조회한다() {
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

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Nested
    @DisplayName("예약 슬롯(날짜, 시간, 테마)을 수정할 수 있다")
    class Update {

        @Test
        void 관리자는_예약일_당일에도_예약_일정을_수정할_수_있다() {
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
                .when().put("/admin/reservations/1")
                .then().log().all()
                .statusCode(200)
                .body("id", is(1))
                .body("date", is("2026-05-02"))
                .body("time.id", is(2));

            Map<String, Object> updatedReservation = jdbcTemplate.queryForMap(
                "SELECT date, time_id FROM reservation WHERE id = ?",
                1L
            );
            assertThat(updatedReservation.get("DATE").toString()).isEqualTo("2026-05-02");
            assertThat(updatedReservation.get("TIME_ID")).isEqualTo(2L);
        }

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
                .put("/admin/reservations/{id}", 1L);

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
                .put("/admin/reservations/{id}", 1L);

            // then
            response.then().log().all()
                .statusCode(HttpStatus.OK.value());

            assertThat(findReservationNameBySlot(newSlotDate, 1L, 1L)).isEqualTo("customer");
            assertThat(countWaitings()).isEqualTo(1);
        }

    }

    @Nested
    @DisplayName("예약을 삭제한다")
    class DeleteReservation {

        @Test
        void 관리자는_예약을_삭제할_수_있다() {
            // given
            insertReservationTime("10:00");
            insertTheme("링", "공포 테마", "http:~");
            insertReservation("브라운", "2026-08-05", 1L, 1L);

            // when
            final Response response = RestAssured.given().log().all()
                .when().delete("/admin/reservations/1");

            // then
            response.then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(countReservations()).isZero();
        }

        @Test
        void 존재하지_않는_예약을_관리자가_삭제하면_404를_응답한다() {
            // given
            final long unsavedReservationId = 999L;

            // when
            final Response response = RestAssured.given().log().all()
                .when().delete("/admin/reservations/{id}", unsavedReservationId);

            // then
            response.then().log().all()
                .statusCode(404)
                .body("message", is("존재하지 않는 예약입니다."));
        }

        @Test
        void 관리자가_미래_예약을_삭제하면_가장_빠른_대기자가_예약으로_전환된다() {
            // given
            insertReservationTime("10:00");
            insertTheme("링", "공포 테마", "http:~");
            insertReservation("브라운", "2026-08-05", 1, 1);
            insertWaiting("코로구", "2026-08-05", 1, 1);

            // when
            final Response response = RestAssured.given().log().all()
                .when().delete("/admin/reservations/1");

            // then
            response.then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(countReservations()).isOne();
            assertThat(countWaitings()).isZero();
        }

        @Test
        void 관리자가_과거_예약을_삭제해도_대기자는_예약으로_전환되지_않는다() {
            // given
            insertReservationTime("10:00");
            insertTheme("링", "공포 테마", "http:~");
            insertReservation("브라운", "2026-04-30", 1L, 1L);
            insertWaiting("코로구", "2026-04-30", 1L, 1L);

            // when
            final Response response = RestAssured.given().log().all()
                .when().delete("/admin/reservations/1");

            // then
            response.then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(countReservations()).isZero();
            assertThat(countWaitings()).isOne();
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
                .delete("/admin/reservations/{id}", 1L);

            // then
            response.then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

            assertThat(countReservations()).isZero();
            assertThat(countWaitings()).isEqualTo(1);
        }
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

    private void insertWaiting(final String name, final String date, final long timeId, final long themeId) {
        jdbcTemplate.update("""
                INSERT INTO waiting (customer_name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)
                """,
            name,
            date,
            timeId,
            themeId
        );
    }

    private void insertReservationTime(final String startAt) {
        jdbcTemplate.update("""
                INSERT INTO reservation_time (start_at) VALUES (?)
                """,
            startAt
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
}
