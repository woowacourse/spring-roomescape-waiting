package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ClearDbTest;
import roomescape.dto.request.ReservationTimeCreateRequest;
import roomescape.dto.response.ReservationTimeResult;

@ClearDbTest
class ReservationTimeControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Nested
    class 예약_시간_생성 {

        @Test
        void 성공() {
            LocalTime startAt = LocalTime.of(12, 0);
            LocalTime endAt = LocalTime.of(12, 30);
            ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(startAt, endAt);

            ReservationTimeResult response = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(201)
                    .header("Location", "/times/1")
                    .extract().as(ReservationTimeResult.class);

            assertThat(response.id()).isEqualTo(1);
            assertThat(response.startAt()).isEqualTo(startAt);
            assertThat(response.endAt()).isEqualTo(endAt);

            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM reservation_time", Integer.class);
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    class 예약_시간_조회 {

        @Test
        void 전체_예약시간_반환() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "11:00", "11:30");

            List<ReservationTimeResult> reservationTimes = RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", ReservationTimeResult.class);

            assertThat(reservationTimes).hasSize(2);

            ReservationTimeResult reservationTime1 = reservationTimes.getFirst();
            assertThat(reservationTime1.id()).isEqualTo(1);
            assertThat(reservationTime1.startAt()).isEqualTo(LocalTime.of(10, 0));
            assertThat(reservationTime1.endAt()).isEqualTo(LocalTime.of(10, 30));

            ReservationTimeResult reservationTime2 = reservationTimes.get(1);
            assertThat(reservationTime2.id()).isEqualTo(2);
            assertThat(reservationTime2.startAt()).isEqualTo(LocalTime.of(11, 0));
            assertThat(reservationTime2.endAt()).isEqualTo(LocalTime.of(11, 30));
        }

        @Test
        void 저장된_시간이_없으면_빈_목록_반환() {
            List<ReservationTimeResult> reservationTimes = RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200).extract()
                    .jsonPath().getList(".", ReservationTimeResult.class);

            assertThat(reservationTimes).isEmpty();
        }
    }

    @Nested
    class 예약_시간_삭제 {

        @Test
        void 성공() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");

            RestAssured.given().log().all()
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(204);

            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM reservation_time", Integer.class);
            assertThat(count).isZero();
        }

        @Test
        void 예약이_있는_시간_삭제_시도시_409() {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)", "10:00", "10:30");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "링", "공포 테마", "http:~");
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                    "브라운", LocalDate.now().plusDays(1), "1", "1");

            RestAssured.given().log().all()
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(409);
        }
    }
}
