package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

class TimeAcceptanceTest extends AcceptanceFixture {

    @Test
    @DisplayName("예약 시간 생성 API")
    void reservation_time_generation_API() {
        // given
        Map<String, LocalTime> startAt = Map.of("startAt", LocalTime.of(1, 0));

        // when & then
        RestAssured
                .given().contentType(ContentType.JSON).body(startAt)
                .when().post("/times")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1))
                .log().all();
    }

    @Test
    @DisplayName("예약 시간 조회 API")
    void reservation_time_inquiry_API() {
        // given
        Map<String, LocalTime> startAt1 = Map.of("startAt", LocalTime.of(1, 0));
        Map<String, LocalTime> startAt2 = Map.of("startAt", LocalTime.of(2, 0));

        RestAssured
                .given().contentType(ContentType.JSON).body(startAt1)
                .when().post("/times")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1))
                .log().all();

        RestAssured
                .given().contentType(ContentType.JSON).body(startAt2)
                .when().post("/times")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(2))
                .log().all();

        // when & then
        RestAssured
                .given()
                .when().get("/times")
                .then().statusCode(HttpStatus.SC_OK)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("예약 시간 삭제 API")
    void reservation_time_remove_API() {
        // given
        Map<String, LocalTime> startAt1 = Map.of("startAt", LocalTime.of(1, 0));
        Map<String, LocalTime> startAt2 = Map.of("startAt", LocalTime.of(2, 0));
        RestAssured
                .given().contentType(ContentType.JSON).body(startAt1)
                .when().post("/times")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1))
                .log().all();

        RestAssured
                .given().contentType(ContentType.JSON).body(startAt2)
                .when().post("/times")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(2))
                .log().all();

        // when
        RestAssured
                .given()
                .when().delete("/times/1")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);

        // then
        RestAssured
                .given()
                .when().get("/times")
                .then().statusCode(HttpStatus.SC_OK)
                .body("size()", is(1));
    }

    // GET localhost:8080/times/available?date=2024-10-11&theme-id=1
    @Test
    @DisplayName("예약 가능 시간 조회 API")
    void inquire_available_time_API() {
        // given
        ReservationTime time1 = timeRepository.save(new ReservationTime(LocalTime.of(1, 0)));
        ReservationTime time2 = timeRepository.save(new ReservationTime(LocalTime.of(2, 0)));
        ReservationTime time3 = timeRepository.save(new ReservationTime(LocalTime.of(3, 0)));
        Member member = memberRepository.save(new Member("aa", "aa@aa.aa", "aa"));
        Theme theme = themeRepository.save(new Theme("n", "d", "t"));

        reservationRepository.save(new Reservation(LocalDate.of(2023, 12, 11), time1, theme, member));
        reservationRepository.save(new Reservation(LocalDate.of(2023, 12, 12), time2, theme, member));

        // when
        RestAssured
                .given()
                .when().get("/times/available?date=2023-12-11&theme-id=1")
                .then().statusCode(HttpStatus.SC_OK)
                .body("[0].alreadyBooked", is(true))
                .body("[1].alreadyBooked", is(false))
                .body("[2].alreadyBooked", is(false))
                .log().all();
    }
}
