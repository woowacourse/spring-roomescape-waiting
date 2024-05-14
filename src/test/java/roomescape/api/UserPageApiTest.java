package roomescape.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static roomescape.LoginTestSetting.getCookieByLogin;

import io.restassured.http.Cookie;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

@Sql("/page-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserPageApiTest {

    private static final String ADMIN_EMAIL = "admin@email.com";
    private static final String ADMIN_PASSWORD = "123456";

    private static final String USER_EMAIL = "test@email.com";
    private static final String USER_PASSWORD = "123456";

    @LocalServerPort
    int port;

    @ParameterizedTest
    @ValueSource(strings = {"/login", "/"})
    void 로그인_페이지와_인기테마_페이지는_로그인_하지_않아도_진입_가능(String url) {
        given().log().all()
                .port(port)
                .when().get(url)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 로그인이_되어있지_않은_경우_사용자_예약_페이지_진입시_로그인_페이지로_이동() {
        given().redirects().follow(false).log().all()
                .port(port)
                .when().get("/reservation")
                .then().log().all()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    void 로그인_계정의_권한이_사용자가_아닐때_사용자_예약_페이지_진입시_권한없음_응답() {
        Cookie cookieByLogin = getCookieByLogin(port, ADMIN_EMAIL, ADMIN_PASSWORD);

        given().log().all()
                .port(port)
                .cookie(cookieByLogin)
                .when().get("/reservation")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 로그인_계정의_권한이_사용자이면_사용자_페이지_정상_진입() {
        Cookie cookieByLogin = getCookieByLogin(port, USER_EMAIL, USER_PASSWORD);

        given().log().all()
                .port(port)
                .cookie(cookieByLogin)
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }
}
