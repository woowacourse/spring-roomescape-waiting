package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.reservation.dto.ReservationRequest;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@Sql("/truncate-with-time-and-theme.sql")
class ReservationAcceptanceTest extends AcceptanceTest {
    private LocalDate date;
    private long timeId;
    private long themeId;
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
                            .cookie("token", adminToken)
                            .when().get("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(1));
                })
        );
    }

    @DisplayName("예약을 취소한 상태에서 예약 요청을 보내면 예약된다.")
    @TestFactory
    Stream<DynamicTest> deleteAndCreateReservation() {
        AtomicLong reservationId = new AtomicLong();
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    reservationId.set((int) RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().jsonPath().get("id"));
                }),
                DynamicTest.dynamicTest("어드민이 guest1의 예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().delete("/admin/reservations/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("guest1이 다시 예약을 요청하면, 예약으로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                })
        );
    }

    @DisplayName("예약 대기자가 있던 상황에서 예약을 취소한 후, 다시 요청하면 예약 대기로 생성된다")
    @TestFactory
    Stream<DynamicTest> deleteAndCreateWaiting() {
        AtomicLong reservationId = new AtomicLong();
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 요청한 일정과 테마로 예약이 존재하지 않아서 예약 상태로 생성한다.", () -> {
                    reservationId.set((int) RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().extract().jsonPath().get("id"));
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
                            .when().delete("/admin/reservations/" + reservationId)
                            .then().log().all()
                            .assertThat().statusCode(204);
                }),
                DynamicTest.dynamicTest("guest2의 예약 대기가 예약으로 변경되었다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guest2Token)
                            .when().get("/members/reservations")
                            .then().log().all()
                            .assertThat().statusCode(200)
                            .body("[0].reservationStatus.status", is("예약"))
                            .body("[0].reservationStatus.rank", is(0));
                }),
                DynamicTest.dynamicTest("guest1이 다시 예약을 요청하면, 예약 대기로 생성된다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약대기"));
                })
        );
    }

    @DisplayName("예약 요청 시, 동일한 테마와 일정으로 예약 존재 여부에 따라 예약/예약 대기 상태로 생성된다.")
    @TestFactory
    Stream<DynamicTest> createReservationByWaitingOrReserved() {
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
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약대기"));
                })
        );
    }


    @DisplayName("이미 예약이 있는데, 또 예약 요청을 하면 예외를 발생시킨다.")
    @TestFactory
    Stream<DynamicTest> cannotCreateReservationBecauseAlreadyReserved() {
        return Stream.of(
                DynamicTest.dynamicTest("guest1이 예약을 생성한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약"));
                }),
                DynamicTest.dynamicTest("guest1이 동일한 테마와 일정으로 다시 예약을 요청하면 예외가 발생한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest1Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(400).body("message", is("이미 예약(대기) 상태입니다."));
                })
        );
    }

    @DisplayName("이미 예약 대기가 있는데, 또 예약 대기 요청을 하면 예외를 발생시킨다.")
    @TestFactory
    Stream<DynamicTest> cannotCreateReservationBecauseAlreadyWaiting() {
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
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(201).body("status", is("예약대기"));
                }),
                DynamicTest.dynamicTest("guest2가 동일한 테마와 일정으로 다시 예약을 요청하면 예외가 발생한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", guest2Token)
                            .body(new ReservationRequest(date, timeId, themeId))
                            .when().post("/reservations")
                            .then().log().all()
                            .assertThat().statusCode(400).body("message", is("이미 예약(대기) 상태입니다."));
                })
        );
    }
}
