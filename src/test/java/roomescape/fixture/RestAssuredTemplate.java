package roomescape.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import roomescape.auth.dto.LoginRequest;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.time.dto.TimeCreateRequest;
import roomescape.time.dto.TimeResponse;
import roomescape.waiting.dto.WaitingCreateRequest;
import roomescape.waiting.dto.WaitingResponse;

public class RestAssuredTemplate {
    public static Cookies makeUserCookie(Member member) {
        LoginRequest request = new LoginRequest(member.getEmail(), member.getPassword());

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().detailedCookies();
    }

    public static ThemeResponse create(ThemeCreateRequest params, Cookies cookies) {
        return RestAssured.given().log().all()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getObject("", ThemeResponse.class);
    }

    public static TimeResponse create(TimeCreateRequest params, Cookies cookies) {
        return RestAssured.given().log().all()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getObject("", TimeResponse.class);
    }

    public static ReservationResponse create(ReservationCreateRequest params, Cookies cookies) {
        return RestAssured.given().log().all()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getObject("", ReservationResponse.class);
    }

    public static WaitingResponse create(WaitingCreateRequest params, Cookies cookies) {
        return RestAssured.given().log().all()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getObject("", WaitingResponse.class);
    }

    public static void delete(String uri) {
        RestAssured.given().log().all()
                .when().delete(uri)
                .then().log().all()
                .statusCode(204);
    }
}
