package roomescape.acceptance;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import roomescape.service.reservation.dto.request.ReservationRequest;
import roomescape.service.reservation.dto.request.ReservationTimeRequest;
import roomescape.service.reservation.dto.request.ThemeRequest;
import roomescape.service.reservation.dto.request.UserReservationRequest;
import roomescape.service.reservation.dto.request.WaitingRequest;

class WaitingTest extends AcceptanceTest {

    @BeforeEach
    void insert() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", adminToken)
                .body(reservationTimeRequest)
                .post("/times");

        ReservationTimeRequest reservationTimeRequest2 = new ReservationTimeRequest(LocalTime.of(10, 10));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", adminToken)
                .body(reservationTimeRequest)
                .post("/times");

        ThemeRequest themeRequest = new ThemeRequest("hi", "happy", "abcd.html");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", adminToken)
                .body(themeRequest)
                .post("/themes");
    }

    @DisplayName("예약 대기/삭제 테스트")
    @TestFactory
    Stream<DynamicTest> reservationByAdmin() {
        return Stream.of(
                dynamicTest("예약을 추가한다.", () -> {
                    ReservationRequest reservationRequest = new ReservationRequest(
                            LocalDate.of(2099, 12, 12), 1L, 1L, 1L
                    );

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(reservationRequest)
                            .when().post("/admin/reservations")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));
                }),

                dynamicTest("회원이 예약 대기를 한다.", () -> {
                    WaitingRequest waitingRequest = new WaitingRequest(
                            LocalDate.of(2099, 12, 12), 1L, 1L
                    );

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(waitingRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));
                }),

                dynamicTest("회원이 예약 대기를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", userToken)
                            .when().delete("/waitings/1")
                            .then().log().all()
                            .statusCode(204);
                })
        );
    }

    @DisplayName("회원은 동일 날짜, 테마에 예약 존재시 예약 대기 불가능")
    @TestFactory
    Stream<DynamicTest> validateMemberReservationAlreadyExist() {
        return Stream.of(
                dynamicTest("회원이 예약을 추가한다.", () -> {
                    UserReservationRequest userReservationRequest = new UserReservationRequest(
                            LocalDate.of(2099, 12, 12), 1L, 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));
                }),

                dynamicTest("회원이 동일 날짜, 테마에 예약 대기를 시도한다.", () -> {
                    WaitingRequest waitingRequest = new WaitingRequest(
                            LocalDate.of(2099, 12, 12), 1L, 2L
                    );

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(waitingRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(400);
                })
        );
    }

    @DisplayName("회원은 동일 날짜, 시간, 테마에 중복 예약 대기를 할 수 없다.")
    @TestFactory
    Stream<DynamicTest> validateMemberWaitingAlreadyExist() {
        return Stream.of(
                dynamicTest("예약을 추가한다.", () -> {
                    ReservationRequest reservationRequest = new ReservationRequest(
                            LocalDate.of(2099, 12, 12), 1L, 1L, 1L
                    );

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(reservationRequest)
                            .when().post("/admin/reservations")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));
                }),

                dynamicTest("회원이 예약 대기를 한다.", () -> {
                    WaitingRequest waitingRequest = new WaitingRequest(
                            LocalDate.of(2099, 12, 12), 1L, 1L
                    );

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(waitingRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));
                }),

                dynamicTest("회원이 동일 날짜, 시간, 테마에 예약 대기를 한다.", () -> {
                    WaitingRequest waitingRequest = new WaitingRequest(
                            LocalDate.of(2099, 12, 12), 1L, 1L
                    );

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(waitingRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(400);
                })
        );
    }
}
