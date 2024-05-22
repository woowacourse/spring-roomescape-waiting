package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.token.TokenProvider;
import roomescape.member.model.MemberRole;
import roomescape.reservation.dto.SaveReservationWaitingRequest;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationWaitingControllerTest {

    @Autowired
    private TokenProvider tokenProvider;

    @LocalServerPort
    int randomServerPort;

    @BeforeEach
    public void initReservation() {
        RestAssured.port = randomServerPort;
    }

    @DisplayName("예약 대기 정보를 모두 조회한다.")
    @Test
    void getMyReservationWaitingTest() {
        // When & Then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(1L, MemberRole.ADMIN))
                .when().get("/reservation-waiting-mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @DisplayName("예약 대기 정보를 저장한다.")
    @Test
    void saveReservationWaitingTest() {
        // Given
        final SaveReservationWaitingRequest saveReservationWaitingRequest = new SaveReservationWaitingRequest(LocalDate.now().plusDays(3), 2L, 1L, 10L);

        // When & Then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(2L, MemberRole.USER))
                .body(saveReservationWaitingRequest)
                .when().post("/reservation-waiting")
                .then().log().all()
                .statusCode(201)
                .body("id", is(7));
    }

    @DisplayName("존재하지 않는 예약에 대해 대기 신청을 하면 에러 코드가 반환된다.")
    @Test
    void saveNotExistReservationWaitingTest() {
        // Given
        final SaveReservationWaitingRequest saveReservationWaitingRequest = new SaveReservationWaitingRequest(LocalDate.now(), 1L, 1L, 2L);

        // When & Then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(2L, MemberRole.USER))
                .body(saveReservationWaitingRequest)
                .when().post("/reservation-waiting")
                .then().log().all()
                .statusCode(400)
                .body("message", is("존재하지 않는 예약에 대한 대기 신청을 할 수 없습니다."));
    }

    @DisplayName("중복된 예약 대기 신청을 하면 에러 코드가 반환된다.")
    @Test
    void saveDuplicateReservationWaitingTest() {
        // Given
        final SaveReservationWaitingRequest saveReservationWaitingRequest = new SaveReservationWaitingRequest(LocalDate.now().plusDays(6), 2L, 3L, 8L);

        // When & Then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(2L, MemberRole.USER))
                .body(saveReservationWaitingRequest)
                .when().post("/reservation-waiting")
                .then().log().all()
                .statusCode(400)
                .body("message", is("이미 해당 예약 대기가 존재합니다."));
    }

    @DisplayName("예약 대기 정보를 삭제한다.")
    @Test
    void deleteReservationWaitingTest() {
        // When & Then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", createAccessToken(2L, MemberRole.USER))
                .when().delete("/reservation-waiting/" + 2L)
                .then().log().all()
                .statusCode(204);
    }

    private String createAccessToken(final Long memberId, final MemberRole role) {
        return tokenProvider.createToken(memberId, role);
    }
}
