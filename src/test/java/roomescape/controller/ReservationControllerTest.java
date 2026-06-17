package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ClearDbTest;
import roomescape.dto.AvailableDateResult;
import roomescape.dto.ReservationResult;
import roomescape.dto.ReservationTimeStatusResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ClearDbTest
class ReservationControllerTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final String STRING_TOMORROW = TOMORROW.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Nested
    class 예약_추가 {

        @BeforeEach
        void setUp() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        }

        @Test
        void 성공() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "브라운");
            params.put("date", STRING_TOMORROW);
            params.put("timeId", 1);
            params.put("themeId", 1);
            params.put("amount", 50000);

            ReservationResult response = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201).extract()
                    .jsonPath().getObject(".", ReservationResult.class);

            assertThat(response.id()).isEqualTo(1);
            assertThat(response.name()).isEqualTo("브라운");
            assertThat(response.date()).isEqualTo(TOMORROW);
            assertThat(response.time().id()).isEqualTo(1);
            assertThat(response.theme().id()).isEqualTo(1);

            Integer count = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
            assertThat(count).isEqualTo(1);
        }

        @Test
        void 중복_예약_시도시_422() {
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");

            Map<String, Object> params = new HashMap<>();
            params.put("name", "검프");
            params.put("date", STRING_TOMORROW);
            params.put("timeId", 1);
            params.put("themeId", 1);
            params.put("amount", 50000);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(422);
        }
    }

    @Nested
    class 예약_가능_날짜_조회 {

        @Test
        void 오늘부터_14일_반환() {
            AvailableDateResult responses = RestAssured.given().log().all()
                    .when().get("/reservations/available-dates")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getObject(".", AvailableDateResult.class);

            final LocalDate expectedStartDate = TODAY;
            final LocalDate expectedEndDate = expectedStartDate.plusDays(14 - 1);

            final List<LocalDate> actualDates = responses.dates();

            assertThat(actualDates).hasSize(14).doesNotContainAnyElementsOf(
                    List.of(
                            expectedStartDate.minusDays(1),
                            expectedEndDate.plusDays(1)
                    )
            );
        }
    }

    @Nested
    class 예약_가능_시간_조회 {

        @BeforeEach
        void setUp() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "11:00", "11:30");
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "12:00", "12:30");
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "13:00", "13:30");
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "14:00", "14:30");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
        }

        @Test
        void 예약_전_전체_시간이_예약_가능() {
            List<ReservationTimeStatusResult> result = getReservationTimeStatusResponses();
            assertThat(result).hasSize(5);
            assertThat(countReservableTimes(result)).isEqualTo(5);
        }

        @Test
        void 예약_후_해당_시간은_예약_불가() {
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");

            List<ReservationTimeStatusResult> result = getReservationTimeStatusResponses();
            assertThat(result).hasSize(5);
            assertThat(countReservableTimes(result)).isEqualTo(4);
        }

        private List<ReservationTimeStatusResult> getReservationTimeStatusResponses() {
            return RestAssured.given().log().all()
                    .when().get("/reservations/available-times?date=" + STRING_TOMORROW + "&themeId=1")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", ReservationTimeStatusResult.class);
        }

        private int countReservableTimes(final List<ReservationTimeStatusResult> timeStatuses) {
            int count = 0;
            for (final ReservationTimeStatusResult timeStatus : timeStatuses) {
                if (!timeStatus.reserved()) {
                    count++;
                }
            }
            return count;
        }
    }

    @Nested
    class 예약_조회 {

        @Test
        void 본인_이름으로_예약_목록_조회() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");
            jdbcTemplate.update("INSERT INTO orders (order_id, amount, reservation_id, status) VALUES (?, ?, ?, ?)", "order-001", 50000, 1, "PENDING");

            List<ReservationResult> reservations = RestAssured.given().log().all()
                    .when().get("/reservations?name=브라운")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", ReservationResult.class);

            assertThat(reservations).hasSize(1);
            ReservationResult response = reservations.getFirst();
            assertThat(response.id()).isEqualTo(1);
            assertThat(response.name()).isEqualTo("브라운");
            assertThat(response.date()).isEqualTo(TOMORROW);
            assertThat(response.time().id()).isEqualTo(1);
            assertThat(response.theme().id()).isEqualTo(1);
        }

        @Test
        void 없는_이름이면_빈_목록_반환() {
            List<ReservationResult> reservations = RestAssured.given().log().all()
                    .when().get("/reservations?name=없는사람")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", ReservationResult.class);

            assertThat(reservations).isEmpty();
        }
    }

    @Nested
    class 예약_변경 {

        @BeforeEach
        void setUp() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "11:00", "11:30");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");
            jdbcTemplate.update("INSERT INTO orders (order_id, amount, reservation_id, status) VALUES (?, ?, ?, ?)", "order-001", 50000, 1, "PENDING");
        }

        @Test
        void 날짜와_시간_변경_성공() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "브라운");
            params.put("date", STRING_TOMORROW);
            params.put("timeId", 2);
            params.put("themeId", 1);

            ReservationResult reservation = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().patch("/reservations/1")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getObject(".", ReservationResult.class);

            assertThat(reservation.id()).isEqualTo(1);
            assertThat(reservation.name()).isEqualTo("브라운");
            assertThat(reservation.date()).isEqualTo(TOMORROW);
            assertThat(reservation.time().id()).isEqualTo(2);
            assertThat(reservation.theme().id()).isEqualTo(1);
        }

        @Test
        void 타인_예약_변경_시도시_403() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "검프");
            params.put("date", STRING_TOMORROW);
            params.put("timeId", 2);
            params.put("themeId", 1);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().patch("/reservations/1")
                    .then().log().all()
                    .statusCode(403);
        }
    }

    @Nested
    class 예약_취소 {

        @BeforeEach
        void setUp() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운", STRING_TOMORROW, "1", "1");
        }

        @Test
        void 본인_예약_취소_성공() {
            RestAssured.given().log().all()
                    .when().delete("/reservations/1?name=브라운")
                    .then().log().all()
                    .statusCode(204);

            Integer count = jdbcTemplate.queryForObject("SELECT count(*) from reservation", Integer.class);
            assertThat(count).isZero();
        }

        @Test
        void 타인_예약_취소_시도시_403() {
            RestAssured.given().log().all()
                    .when().delete("/reservations/1?name=검프")
                    .then().log().all()
                    .statusCode(403);
        }
    }
}
