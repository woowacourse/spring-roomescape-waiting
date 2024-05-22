package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.application.member.dto.request.MemberRegisterRequest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.request.ThemeRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationStatusResponse;

class ReservationAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservationTest() {
        long themeId = fixture.createTheme(new ThemeRequest("테마명", "테마 설명", "url")).id();
        long timeId = fixture.createReservationTime(10, 0).id();
        fixture.registerMember(new MemberRegisterRequest("name", "email@mail.com", "12341234"));
        String token = fixture.loginAndGetToken("email@mail.com", "12341234");

        ReservationRequest request = new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId);
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("예약 대기를 생성한다.")
    void createWaitingTest() {
        long themeId = fixture.createTheme(new ThemeRequest("name", "desc", "url")).id();
        long timeId = fixture.createReservationTime(10, 0).id();
        fixture.registerMember(new MemberRegisterRequest("name", "email@mail.com", "12341234"));
        String token = fixture.loginAndGetToken("email@mail.com", "12341234");
        fixture.createReservation(token, new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId));

        fixture.registerMember(new MemberRegisterRequest("test", "test@mail.com", "12341234"));
        String waitingToken = fixture.loginAndGetToken("test@mail.com", "12341234");

        ReservationRequest waitingRequest = new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId);
        RestAssured.given().log().all()
                .cookie("token", waitingToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("관리자가 예약을 삭제한다.")
    void deleteReservation() {
        long themeId = fixture.createTheme(new ThemeRequest("name", "desc", "url")).id();
        long timeId = fixture.createReservationTime(10, 0).id();
        fixture.registerMember(new MemberRegisterRequest("name", "email@mail.com", "12341234"));
        String token = fixture.loginAndGetToken("email@mail.com", "12341234");
        ReservationResponse response = fixture.createReservation(
                token,
                new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId)
        );

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/reservations/{id}", response.id())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("사용자가 예약 대기를 삭제한다.")
    void deleteWaiting() {
        long themeId = fixture.createTheme(new ThemeRequest("name", "desc", "url")).id();
        long timeId = fixture.createReservationTime(10, 0).id();

        fixture.registerMember(new MemberRegisterRequest("name", "email@mail.com", "12341234"));
        String reservationToken = fixture.loginAndGetToken("email@mail.com", "12341234");
        fixture.createReservation(reservationToken,
                new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId));

        fixture.registerMember(new MemberRegisterRequest("test", "test@mail.com", "12341234"));
        String waitingToken = fixture.loginAndGetToken("test@mail.com", "12341234");
        ReservationResponse response = fixture.createWaiting(waitingToken,
                new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId));

        RestAssured.given().log().all()
                .cookie("token", waitingToken)
                .when().delete("/reservations/waiting/{id}", response.id())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("나의 예약 목록을 조회한다.")
    void findMyReservations() {
        long themeId = fixture.createTheme(new ThemeRequest("name", "desc", "url")).id();
        long timeId = fixture.createReservationTime(10, 0).id();
        fixture.registerMember(new MemberRegisterRequest("name", "email@mail.com", "12341234"));
        String token = fixture.loginAndGetToken("email@mail.com", "12341234");
        fixture.createReservation(token, new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId));

        ReservationStatusResponse[] responses = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(ReservationStatusResponse[].class);

        assertThat(responses).hasSize(1);
    }
}
