package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.reservation.dto.ReservationRequest;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@Sql("/truncate-with-time-and-theme.sql")
class ReservationControllerTest extends ControllerTest {
    private LocalDate date;
    private long timeId;
    private long themeId;
    private long reservationId;
    private String guest1Token;
    private String guest2Token;
    private String adminToken;

    @BeforeEach
    void init() {
        date = LocalDate.now().plusDays(1);
        timeId = 1;
        themeId = 1;

        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin123", "admin@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");

        guest1Token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("guest123", "guest@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");

        guest2Token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("guest123", "guest2@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");
    }

    @DisplayName("예약 추가 성공 테스트")
    @Test
    void createReservation() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", guest1Token)
                .body(new ReservationRequest(date, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(201).body("id", is(greaterThan(0)));
    }

    @DisplayName("예약 추가 실패 테스트 - 중복 일정 오류")
    @TestFactory
    Stream<DynamicTest> createDuplicatedReservation() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations");
                }),
                DynamicTest.dynamicTest("같은 일정으로 예약 생성을 시도하면 400 응답을 반환한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(400)
                            .body("message", is("선택하신 테마와 일정은 이미 예약이 존재합니다."));
                })
        );
    }

    @DisplayName("예약 추가 실패 테스트 - 일정 오류")
    @Test
    void createInvalidScheduleReservation() {
        //given
        LocalDate invalidDate = LocalDate.now().minusDays(1);

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", guest1Token)
                .body(new ReservationRequest(invalidDate, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(400).body("message", is("현재보다 이전으로 일정을 설정할 수 없습니다."));
    }

    @DisplayName("모든 예약 내역 조회 테스트")
    @TestFactory
    Stream<DynamicTest> findAllReservations() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations");
                }),
                DynamicTest.dynamicTest("모든 예약 내역을 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().get("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(1));
                })
        );
    }

    @DisplayName("예약 취소 성공 테스트")
    @TestFactory
    Stream<DynamicTest> deleteReservationSuccess() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 생성하고, 식별자를 반환한다.", () -> {
                    reservationId = (int) RestAssured.given().contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().body().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().delete("/reservations/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("모든 예약 내역을 조회하면 남은 예약은 0개이다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().get("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(0));
                })
        );
    }

    @DisplayName("예약 취소 실패 테스트 - 본인 예약 아님")
    @TestFactory
    Stream<DynamicTest> cannotDeleteReservationSuccess() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 생성하고, 식별자를 반환한다.", () -> {
                    reservationId = (int) RestAssured.given().contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().body().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("본인이 하지 않은 예약을 삭제하려고 하면 401 응답을 한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().delete("/reservations/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(403).body("message", is("예약을 삭제할 권한이 없습니다."));
                }),
                DynamicTest.dynamicTest("모든 예약 내역을 조회하면 남은 예약은 1개이다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().get("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(1));
                })
        );
    }
}
