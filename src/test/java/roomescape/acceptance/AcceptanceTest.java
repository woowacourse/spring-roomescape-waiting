package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.assertj.core.api.StandardSoftAssertionsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.member.domain.Member;
import roomescape.member.dto.request.MemberJoinRequest;
import roomescape.member.dto.response.MemberResponse;
import roomescape.reservation.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.dto.request.ThemeSaveRequest;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.dto.response.ThemeResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.*;
import static roomescape.member.domain.Role.ADMIN;
import static roomescape.member.domain.Role.USER;

@Sql("/test-schema.sql")
@ActiveProfiles(value = "test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AcceptanceTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    protected Long createTestTheme() {
        ThemeSaveRequest request = new ThemeSaveRequest(WOOTECO_THEME_NAME, WOOTECO_THEME_DESCRIPTION, THEME_THUMBNAIL);
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().extract()
                .as(ThemeResponse.class)
                .id();
    }

    protected Long createTestReservationTime() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(MIA_RESERVATION_TIME);
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().extract()
                .as(ReservationTimeResponse.class)
                .id();
    }

    protected Member createTestMember() {
        MemberJoinRequest request = new MemberJoinRequest(MIA_EMAIL, TEST_PASSWORD, MIA_NAME);
        MemberResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/members/join")
                .then().extract()
                .as(MemberResponse.class);
        return new Member(response.id(), response.name(), response.email(), request.password(), USER);
    }

    protected Member createTestAdmin() {
        MemberJoinRequest request = new MemberJoinRequest(ADMIN_EMAIL, TEST_PASSWORD, ADMIN_NAME);
        MemberResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/members/join/admin")
                .then().extract()
                .as(MemberResponse.class);
        return new Member(response.id(), response.name(), response.email(), request.password(), ADMIN);
    }

    protected String createTestToken(Member member) {
        LoginRequest request = new LoginRequest(member.getEmail(), member.getPassword());
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .getCookie("token");
    }

    protected void checkHttpStatusOk(
            StandardSoftAssertionsProvider softAssertionsProvider, ExtractableResponse<Response> response) {
        softAssertionsProvider.assertThat(response.statusCode())
                .isEqualTo(HttpStatus.OK.value());
    }

    protected void checkHttpStatusCreated(
            StandardSoftAssertionsProvider softAssertionsProvider, ExtractableResponse<Response> response) {
        softAssertionsProvider.assertThat(response.statusCode())
                .isEqualTo(HttpStatus.CREATED.value());
    }

    protected void checkHttpStatusNoContent(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    protected void checkHttpStatusBadRequest(
            StandardSoftAssertionsProvider softAssertionsProvider, ExtractableResponse<Response> response) {
        softAssertionsProvider.assertThat(response.statusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    protected void checkHttpStatusNotFound(
            StandardSoftAssertionsProvider softAssertionsProvider, ExtractableResponse<Response> response) {
        softAssertionsProvider.assertThat(response.statusCode())
                .isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    protected void checkHttpStatusUnauthorized(
            StandardSoftAssertionsProvider softAssertionsProvider, ExtractableResponse<Response> response) {
        softAssertionsProvider.assertThat(response.statusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
