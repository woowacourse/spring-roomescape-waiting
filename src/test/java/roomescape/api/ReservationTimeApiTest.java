package roomescape.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.time.LocalTime;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.reservationtime.ReservationTimeRequest;

@Sql("/reservation-time-api-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTimeApiTest {

    @LocalServerPort
    int port;

    @Test
    void 예약_시간_추가() {
        ReservationTimeRequest reservationTimeRequest = createReservationTimeRequest();

        RestAssured.given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(reservationTimeRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/times/1")
                .body("id", equalTo(1))
                .body("startAt", equalTo(reservationTimeRequest.startAt().toString()));
    }

    @Test
    void 예약_시간_단일_조회() {
        ReservationTimeRequest reservationTimeRequest = createReservationTimeRequest();
        addReservationTime(reservationTimeRequest);

        RestAssured.given().log().all()
                .port(port)
                .when().get("/times/1")
                .then().log().all()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("startAt", equalTo(reservationTimeRequest.startAt().toString())
                );
    }

    @Test
    void 예약_시간_전체_조회() {
        ReservationTimeRequest reservationTimeRequest = createReservationTimeRequest();
        addReservationTime(reservationTimeRequest);

        RestAssured.given().log().all()
                .port(port)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Sql("/reservation-time-service-test-data.sql")
    @Test
    void 예약_가능한_시간_조회() {
        String targetDay = LocalDate.now().plusDays(1).toString();

        RestAssured.given().log().all()
                .port(port)
                .when().get("/times/available?date=" + targetDay + "&themeId=1")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5))
                .body("alreadyBooked", contains(true, true, false, true, false));
    }

    @Test
    void 예약_시간_삭제() {
        ReservationTimeRequest reservationTimeRequest = createReservationTimeRequest();
        addReservationTime(reservationTimeRequest);

        RestAssured.given().log().all()
                .port(port)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    private ReservationTimeRequest createReservationTimeRequest() {
        return new ReservationTimeRequest(LocalTime.parse("11:00"));
    }

    private void addReservationTime(final ReservationTimeRequest reservationTimeRequest) {
        RestAssured.given().log().all()
                .port(port)
                .contentType(ContentType.JSON)
                .body(reservationTimeRequest)
                .when().post("/times");
    }
}
