package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import roomescape.dto.auth.TokenRequest;
import roomescape.dto.auth.TokenResponse;
import roomescape.dto.reservation.MemberReservationSaveRequest;
import roomescape.dto.reservation.ReservationTimeSaveRequest;
import roomescape.dto.theme.ThemeSaveRequest;

import static roomescape.TestFixture.ADMIN_EMAIL;
import static roomescape.TestFixture.DATE_MAY_EIGHTH;
import static roomescape.TestFixture.MEMBER_CAT_EMAIL;
import static roomescape.TestFixture.MEMBER_PASSWORD;
import static roomescape.TestFixture.START_AT_SIX;
import static roomescape.TestFixture.THEME_COMIC_DESCRIPTION;
import static roomescape.TestFixture.THEME_COMIC_NAME;
import static roomescape.TestFixture.THEME_COMIC_THUMBNAIL;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    protected Long saveReservationTime() {
        final ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(START_AT_SIX);

        Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath().get("id");

        return Long.valueOf(id);
    }

    protected Long saveTheme() {
        final ThemeSaveRequest request
                = new ThemeSaveRequest(THEME_COMIC_NAME, THEME_COMIC_DESCRIPTION, THEME_COMIC_THUMBNAIL);

        Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath().get("id");

        return Long.valueOf(id);
    }

    protected Long saveReservation() {
        final Long timeId = saveReservationTime();
        final Long themeId = saveTheme();
        final String accessToken = getAccessToken(MEMBER_CAT_EMAIL);
        final MemberReservationSaveRequest request = new MemberReservationSaveRequest(DATE_MAY_EIGHTH, timeId, themeId);

        Integer id = RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath().get("id");

        return Long.valueOf(id);
    }

    protected String getAccessToken(final String email) {
        return RestAssured.given().log().all()
                .body(new TokenRequest(email, MEMBER_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .extract().as(TokenResponse.class).accessToken();
    }

    protected void assertCreateResponseWithToken(final Object request, final String email, final String path, final int statusCode) {
        final String accessToken = getAccessToken(email);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(path)
                .then().log().all()
                .statusCode(statusCode);
    }

    protected void assertCreateResponse(final Object request, final String path, final int statusCode) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(path)
                .then().log().all()
                .statusCode(statusCode);
    }

    protected void assertGetResponse(final String path, final int statusCode) {
        RestAssured.given().log().all()
                .when().get(path)
                .then().log().all()
                .statusCode(statusCode);
    }

    protected void assertGetResponseWithLoginMember(final String path, final int statusCode) {
        final String accessToken = getAccessToken(ADMIN_EMAIL);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get(path)
                .then().log().all()
                .statusCode(statusCode);
    }

    protected void assertDeleteResponse(final String path, final Long id, final int statusCode) {
        RestAssured.given().log().all()
                .when().delete(path + id)
                .then().log().all()
                .statusCode(statusCode);
    }
}
