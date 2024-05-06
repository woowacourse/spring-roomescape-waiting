package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import roomescape.application.auth.TokenManager;
import roomescape.application.member.dto.request.MemberLoginRequest;
import roomescape.application.member.dto.request.MemberRegisterRequest;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.request.ReservationTimeRequest;
import roomescape.application.reservation.dto.request.ThemeRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationTimeResponse;
import roomescape.application.reservation.dto.response.ThemeResponse;
import roomescape.domain.role.MemberRole;
import roomescape.domain.role.Role;

@TestComponent
public class AcceptanceFixture {

    @Autowired
    private TokenManager tokenManager;

    private String adminToken;

    @PostConstruct
    void createAdminToken() {
        MemberRole adminRole = new MemberRole(0L, "admin", Role.ADMIN);
        adminToken = tokenManager.createToken(adminRole);
    }

    public ExtractableResponse<Response> registerMember(MemberRegisterRequest request) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .extract();
    }

    public String loginAndGetToken(String email, String password) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new MemberLoginRequest(email, password))
                .when().post("/login")
                .then().log().all()
                .extract()
                .cookie("token");
    }

    public String getAdminToken() {
        return adminToken;
    }

    public ThemeResponse createTheme(ThemeRequest request) {
        return RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .extract()
                .as(ThemeResponse.class);
    }

    public ReservationTimeResponse createReservationTime(int hour, int minute) {
        return RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(new ReservationTimeRequest(LocalTime.of(hour, minute)))
                .when().post("/times")
                .then().log().all()
                .extract()
                .as(ReservationTimeResponse.class);
    }

    public ReservationResponse createReservation(String token, ReservationRequest request) {
        return RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .extract()
                .as(ReservationResponse.class);
    }
}
