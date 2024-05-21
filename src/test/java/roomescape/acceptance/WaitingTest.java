package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.request.UserReservationRequest;

class WaitingTest extends AcceptanceTest {

    @Autowired
    WaitingRepository waitingRepository;

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

    @DisplayName("USER 예약 대기 CRUD 테스트")
    @TestFactory
    Stream<DynamicTest> waitingByUser() {
        return Stream.of(
                dynamicTest("사용자가 예약 대기를 추가한다.", () -> {
                    UserReservationRequest userReservationRequest = new UserReservationRequest(
                            LocalDate.of(2099, 12, 12), 1L, 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));

                    assertThat(waitingRepository.findAll()).hasSize(1);
                }),

                dynamicTest("사용자가 예약을 추가한다.", () -> {
                    ReservationRequest reservationRequest = new ReservationRequest(LocalDate.of(2099, 12, 12), 1L, 1L,
                            1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(reservationRequest)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("사용자가 나의 예약 및 예약 대기를 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", userToken)
                            .when().get("/reservations/mine")
                            .then().log().all()
                            .statusCode(200)
                            .body("size()", is(2));
                }),

                dynamicTest("사용자가 예약 대기를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", userToken)
                            .when().delete("/reservations/mine/1")
                            .then().log().all()
                            .statusCode(204);

                    assertThat(waitingRepository.findAll()).isEmpty();
                })
        );
    }

    @DisplayName("ADMIN 예약 대기 CRUD 테스트")
    @TestFactory
    Stream<DynamicTest> waitingByAdmin() {
        return Stream.of(
                dynamicTest("사용자가 예약 대기를 추가한다.", () -> {
                    UserReservationRequest userReservationRequest = new UserReservationRequest(
                            LocalDate.of(2099, 12, 12), 1L, 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("관리자가 예약 대기를 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", adminToken)
                            .when().get("/admin/waitings")
                            .then().log().all()
                            .statusCode(200)
                            .body("size()", is(1));
                }),

                dynamicTest("관리자가 예약 대기를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", adminToken)
                            .when().delete("/admin/waitings/1")
                            .then().log().all()
                            .statusCode(204);

                    assertThat(waitingRepository.findAll()).isEmpty();
                })
        );
    }
}
