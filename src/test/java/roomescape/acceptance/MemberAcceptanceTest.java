package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.reservation.dto.ReservationRequest;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

@Sql("/truncate-with-time-and-theme.sql")
class MemberAcceptanceTest extends AcceptanceTest {
    private String adminToken;
    private String guest1Token;
    private String guest2Token;
    private LocalDate date;
    private long timeId = 1;
    private long themeId = 1;

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

    @DisplayName("모든 사용자 조회 성공 테스트 - 사용자 총 2명")
    @TestFactory
    Stream<DynamicTest> findAllMembers() {
        return Stream.of(
                DynamicTest.dynamicTest("어드민이 로그인한다.", () -> {
                    adminToken = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("admin123", "admin@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("어드민이 모든 사용자 정보를 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().get("/members")
                            .then().log().all()
                            .assertThat().statusCode(HttpStatus.OK.value()).body("size()", is(3));
                })
        );
    }

    @DisplayName("본인의 모든 예약과 예약 대기를 조회한다.")
    @TestFactory
    Stream<DynamicTest> showAllOwnReservationsAndWaitings() {
        LocalDate anotherDate = LocalDate.now().plusWeeks(3);
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 새로운 예약을 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201);
                }),
                DynamicTest.dynamicTest("guest1이 본인의 예약 내역을 조회하면 1개 내역이 조회된다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().get("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(1));
                }),
                DynamicTest.dynamicTest("guest2가 새로운 예약을 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(anotherDate, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest1이 guest2와 동일한 테마와 일정으로 예약을 요청하고, 1번째 예약 대기 상태로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(anotherDate, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약대기"));
                }),
                DynamicTest.dynamicTest("guest1이 본인의 예약 내역을 조회하면 2개 내역이 조회된다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().get("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(2))
                            .body("[0].reservationStatus.status", is("예약"))
                            .body("[0].reservationStatus.rank", is(0))
                            .body("[1].reservationStatus.status", is("예약대기"))
                            .body("[1].reservationStatus.rank", is(1));
                })
        );
    }

    @DisplayName("예약이 취소되면 바로 다음 예약 대기가 예약으로 전환되며, 전환 후 예약 대기를 취소하려고 하면 예외가 발생한다.")
    @TestFactory
    Stream<DynamicTest> changeToReserved() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    RestAssured.given().log().all()
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
                DynamicTest.dynamicTest("어드민이 본인의 예약 내역을 조회하면 2번째 예약 대기로 조회된다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest1Token)
                            .when().get("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200)
                            .body("[0].reservationStatus.status", is("예약"))
                            .body("[0].reservationStatus.rank", is(0));
                })
        );
    }
}
