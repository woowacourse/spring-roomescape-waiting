package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.application.member.dto.request.MemberRegisterRequest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.request.ThemeRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;

public class WaitingAcceptanceTest extends AcceptanceTest {

    private long themeId;
    private long timeId;

    @BeforeEach
    void setData() {
        themeId = fixture.createTheme(new ThemeRequest("name", "desc", "url")).id();
        timeId = fixture.createReservationTime(10, 0).id();
        fixture.registerMember(new MemberRegisterRequest("name", "email@mail.com", "12341234"));
        String token = fixture.loginAndGetToken("email@mail.com", "12341234");
        fixture.createReservation(token, new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId));
    }

    @Test
    @DisplayName("예약 대기를 생성한다.")
    void createWaitingTest() {
        fixture.registerMember(new MemberRegisterRequest("test", "test@mail.com", "12341234"));
        String waitingToken = fixture.loginAndGetToken("test@mail.com", "12341234");

        ReservationRequest waitingRequest = new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId);
        RestAssured.given().log().all()
                .cookie("token", waitingToken)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("사용자가 예약 대기를 삭제한다.")
    void deleteWaiting() {
        fixture.registerMember(new MemberRegisterRequest("test", "test@mail.com", "12341234"));
        String waitingToken = fixture.loginAndGetToken("test@mail.com", "12341234");
        ReservationResponse response = fixture.createWaiting(waitingToken,
                new ReservationRequest(LocalDate.of(2024, 12, 25), timeId, themeId));

        RestAssured.given().log().all()
                .cookie("token", waitingToken)
                .when().delete("/waiting/{id}", response.id())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("관리자가 예약 대기 목록을 조회한다.")
    void adminReadWaiting() {
        RestAssured.given().log().all()
                .cookie("token", fixture.getAdminToken())
                .when().get("/waiting")
                .then().log().all()
                .statusCode(200);
    }
}
