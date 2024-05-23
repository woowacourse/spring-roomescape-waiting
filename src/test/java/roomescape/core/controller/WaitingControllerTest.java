package roomescape.core.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.core.dto.reservation.MemberReservationRequest;
import roomescape.core.dto.waiting.MemberWaitingRequest;
import roomescape.utils.AccessTokenGenerator;
import roomescape.utils.AdminGenerator;
import roomescape.utils.DatabaseCleaner;
import roomescape.utils.ReservationRequestGenerator;
import roomescape.utils.ReservationTimeRequestGenerator;
import roomescape.utils.TestFixture;
import roomescape.utils.ThemeRequestGenerator;

@AcceptanceTest
class WaitingControllerTest {
    private static final String TOMORROW = TestFixture.getTomorrowDate();

    private String accessToken;

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private AdminGenerator adminGenerator;

    @BeforeEach
    void setUp() {
        databaseCleaner.executeTruncate();
        RestAssured.port = port;

        adminGenerator.generate();
        accessToken = AccessTokenGenerator.generate();

        ThemeRequestGenerator.generateWithName("테마 1");
        ReservationTimeRequestGenerator.generateOneMinuteAfter();
        ReservationRequestGenerator.generateWithTimeAndTheme(1L, 1L);
    }

    @Test
    @DisplayName("예약 대기를 생성할 수 있다.")
    void createWaiting() {
        MemberWaitingRequest request = new MemberWaitingRequest(TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("이미 예약한 내역이 존재하면 예약 대기를 생성할 수 없다.")
    void createWaitingAlreadyHaveReservation() {
        MemberReservationRequest reservationRequest = new MemberReservationRequest(TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        MemberWaitingRequest waitingRequest = new MemberWaitingRequest(TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("이미 예약 대기한 내역이 존재하면 예약 대기를 생성할 수 없다.")
    void createDuplicateWaitingBySameMember() {
        MemberWaitingRequest waitingRequest = new MemberWaitingRequest(TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("예약 대기를 취소할 수 있다.")
    void deleteWaiting() {
        MemberWaitingRequest waitingRequest = new MemberWaitingRequest(TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().delete("/waitings/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("전체 예약 대기 목록을 조회할 수 있다.")
    void findAllWaitings() {
        MemberWaitingRequest waitingRequest = new MemberWaitingRequest(TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }
}
