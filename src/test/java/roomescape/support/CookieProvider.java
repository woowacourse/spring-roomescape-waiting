package roomescape.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import roomescape.support.dto.TokenCookieDto;
import roomescape.support.fixture.MemberFixture;

import java.util.Map;

@Component
public class CookieProvider {

    @Autowired
    private MemberFixture memberFixture;

    public TokenCookieDto saveMemberAndGetJwtTokenCookies(final String email, final String password, final int port) {
        memberFixture.createMember(email, password);

        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login");

        String accessToken = response
                .then().log().all().extract().cookie("accessToken");
        String refreshToken = response
                .then().log().all().extract().cookie("refreshToken");

        return new TokenCookieDto("accessToken=" + accessToken, "refreshToken=" + refreshToken);
    }

    public TokenCookieDto saveAdminAndGetTokenCookies(final String email, final String password, final int port) {
        memberFixture.createAdmin(email, password);

        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login");

        String accessToken = response
                .then().log().all().extract().cookie("accessToken");
        String refreshToken = response
                .then().log().all().extract().cookie("refreshToken");

        return new TokenCookieDto("accessToken=" + accessToken, "refreshToken=" + refreshToken);
    }

    public TokenCookieDto loginAndGetTokenCookies(final String email, final String password, int port) {
        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login");

        String accessToken = response
                .then().log().all().extract().cookie("accessToken");
        String refreshToken = response
                .then().log().all().extract().cookie("refreshToken");

        return new TokenCookieDto("accessToken=" + accessToken, "refreshToken=" + refreshToken);
    }
}
