package roomescape.controller;

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
}
