package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.test.context.jdbc.Sql;
import roomescape.acceptance.AcceptanceTest;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.reservation.dto.ReservationRequest;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

@Sql("/truncate-with-time-and-theme.sql")
class WaitingAcceptanceTest extends AcceptanceTest {
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

    @DisplayName("모든 예약 대기 내역 조회 테스트")
    @TestFactory
    Stream<DynamicTest> findAllWaitings() {
        return Stream.of(
                DynamicTest.dynamicTest("예약을 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations");
                }),
                DynamicTest.dynamicTest("guest2가 guest1과 동일한 테마와 일정으로 예약을 요청하고, 1번째 예약 대기 상태로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().body("status", is("예약대기"));
                }),
                DynamicTest.dynamicTest("모든 예약 대기 내역을 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().get("/waitings")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(1));
                })
        );
    }


    @DisplayName("사용자는 본인의 것이 아닌 예약 대기를 삭제할 수 없다.")
    @TestFactory
    Stream<DynamicTest> cannotDeleteOtherWaiting() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest2가 guest1과 동일한 테마와 일정으로 예약을 요청하고, 1번째 예약 대기 상태로 생성된다.", () -> {
                    reservationId = (int) RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().body().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("guest1이 guest2의 예약 대기를 삭제하려고 하면 예외가 발생한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().delete("/waitings/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(403).body("message",is("예약 대기를 삭제할 권한이 없습니다."));
                })
        );
    }

    @DisplayName("사용자는 예약으로 전환된 예약 대기는 삭제할 수 없다.")
    @TestFactory
    Stream<DynamicTest> cannotDeleteChangedToReservedByGuest() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    reservationId = (int) RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("guest2가 guest1과 동일한 테마와 일정으로 예약을 요청하고, 1번째 예약 대기 상태로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약대기"));
                }),
                DynamicTest.dynamicTest("어드민이 guest1의 예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().delete("/reservations/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("guest2의 예약 대기가 예약으로 변경되었다.", () -> {
                    reservationId = (int) RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().get("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200)
                            .body("[0].reservationStatus.status", is("예약"))
                            .body("[0].reservationStatus.rank", is(0))
                            .extract().jsonPath().get("[0].reservationId");
                }),
                DynamicTest.dynamicTest("guest2가 예약 대기를 삭제하려고 했는데, 이미 예약으로 전환되어 삭제할 수 없다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().delete("/waitings/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(400).body("message",is("예약은 삭제할 수 없습니다. 관리자에게 문의해주세요."));
                })
        );
    }

    @DisplayName("어드민은 예약으로 전환된 예약 대기는 삭제할 수 없다.")
    @TestFactory
    Stream<DynamicTest> cannotDeleteChangedToReservedByAdmin() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    reservationId = (int) RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("guest2가 guest1과 동일한 테마와 일정으로 예약을 요청하고, 1번째 예약 대기 상태로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약대기"));
                }),
                DynamicTest.dynamicTest("어드민이 guest1의 예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().delete("/reservations/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("guest2의 예약 대기가 예약으로 변경되었다.", () -> {
                    reservationId = (int) RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().get("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200)
                            .body("[0].reservationStatus.status", is("예약"))
                            .body("[0].reservationStatus.rank", is(0))
                            .extract().jsonPath().get("[0].reservationId");
                }),
                DynamicTest.dynamicTest("어드민이 예약 대기를 삭제하려고 했는데, 이미 예약으로 전환되어 삭제할 수 없다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().delete("/waitings/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(400).body("message",is("예약은 삭제할 수 없습니다. 관리자에게 문의해주세요."));
                })
        );
    }

    @DisplayName("예약이 취소되면 바로 다음 예약 대기가 예약으로 전환되며, 전환 후 예약 대기를 취소하려고 하면 예외가 발생한다.")
    @TestFactory
    Stream<DynamicTest> changeToReserved() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    reservationId = (int) RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().body().jsonPath().get("id");
                }),
                DynamicTest.dynamicTest("guest2가 guest1과 동일한 테마와 일정으로 예약을 요청하고, 1번째 예약 대기 상태로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약대기"));
                }),
                DynamicTest.dynamicTest("admin이 guest1과 동일한 테마와 일정으로 예약을 요청하고, 2번째 예약 대기 상태로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", adminToken)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약대기"));
                }),
                DynamicTest.dynamicTest("어드민이 guest1의 예약을 취소한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().delete("/reservations/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("guest2의 예약 대기가 예약으로 변경되었다.", () -> {
                    reservationId = (int) RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().get("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200)
                            .body("[0].reservationStatus.status", is("예약"))
                            .body("[0].reservationStatus.rank", is(0))
                            .extract().jsonPath().get("[0].reservationId");
                }),
                DynamicTest.dynamicTest("guest2가 예약 대기를 삭제하려고 하면, 이미 예약으로 전환되어서 예외가 발생한다", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().delete("/waitings/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(400).body("message", is("예약은 삭제할 수 없습니다. 관리자에게 문의해주세요."));
                })
        );
    }
}
