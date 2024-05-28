package roomescape.acceptance;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.request.ThemeRequest;

class ReservationTest extends AcceptanceTest {

    @BeforeEach
    void insert() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", adminToken)
                .body(reservationTimeRequest)
                .post("/times")
                .then().log().all()
                .extract();

        ThemeRequest themeRequest = new ThemeRequest("hi", "happy", "abcd.html");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", adminToken)
                .body(themeRequest)
                .post("/themes");
    }

    @DisplayName("ADMIN 예약 CRUD 테스트")
    @TestFactory
    Stream<DynamicTest> reservationByAdmin() {
        return Stream.of(
                dynamicTest("예약을 추가한다.", () -> {
                    ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2099, 12, 12), 1L, 1L,
                            1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(reservationRequest)
                            .when().post("/admin/reservations")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));
                }),

                dynamicTest("예약을 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", adminToken)
                            .when().get("/reservations")
                            .then().log().all()
                            .statusCode(200)
                            .body("size()", is(1));
                }),

                dynamicTest("예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", adminToken)
                            .when().delete("/reservations/1")
                            .then().log().all()
                            .statusCode(204);
                })
        );
    }

    @DisplayName("USER 예약 조회 CRUD 테스트")
    @TestFactory
    Stream<DynamicTest> reservationByUser() {
        return Stream.of(
                dynamicTest("예약 시간을 추가한다.", () -> {
                    ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(12, 0));
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(reservationTimeRequest)
                            .post("/times")
                            .then().log().all()
                            .extract();
                }),

                dynamicTest("asd 사용자 예약을 추가한다.", () -> {
                    ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2099, 12, 12), 1L, 1L,
                            2L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(reservationRequest)
                            .when().post("/admin/reservations")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));
                }),

                dynamicTest("qwe 사용자 예약을 추가한다.", () -> {
                    ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2099, 12, 12), 2L, 1L,
                            3L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(reservationRequest)
                            .when().post("/admin/reservations")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(2));
                }),

                dynamicTest("asd 사용자의 예약을 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .get("/reservations/mine")
                            .then().log().all()
                            .statusCode(200)
                            .body("size()", is(1));
                })
        );
    }

    @DisplayName("동일한 날짜와 시간에 중복 예약시 예외 처리")
    @TestFactory
    Stream<DynamicTest> duplicateReservation() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "1234567890");
        reservationRequest.put("date", "2030-12-12");
        reservationRequest.put("timeId", "1");
        reservationRequest.put("themeId", "1");

        return Stream.of(
                dynamicTest("예약을 추가한다.", () -> {

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(reservationRequest)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("중복된 예약을 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(reservationRequest)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(400);
                })
        );
    }

    @DisplayName("올바르지 않은 날짜 형식으로 입력시 예외처리")
    @Test
    void invalidDateFormat() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "1234567890");
        reservationRequest.put("date", "2025-aa-bb");
        reservationRequest.put("timeId", "1");
        reservationRequest.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", userToken)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("올바르지 않은 예약자명 형식으로 입력시 예외처리")
    @Test
    void invalidTimeIdFormat() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "12345678900");
        reservationRequest.put("date", "2030-12-12");
        reservationRequest.put("timeId", "a");
        reservationRequest.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", userToken)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("지나간 시점에 대한 예약시 예외처리")
    @Test
    void pastTimeSlotReservation() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "1234567890");
        reservationRequest.put("date", "1999-12-12");
        reservationRequest.put("timeId", "1");
        reservationRequest.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", userToken)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
