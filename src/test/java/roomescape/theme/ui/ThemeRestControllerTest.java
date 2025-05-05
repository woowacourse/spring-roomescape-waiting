package roomescape.theme.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.ui.LoginApiFixture;
import roomescape.fixture.ui.MemberApiFixture;
import roomescape.fixture.ui.ThemeApiFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ThemeRestControllerTest {

    @Test
    void 관리자_권한으로_테마를_추가한다() {
        final Map<String, String> cookies = LoginApiFixture.adminLoginAndGetCookies();

        final Map<String, String> themeParams = ThemeApiFixture.themeParams1();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(themeParams)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 회원_권한으로_테마_추가를_시도하면_UNATHORIZED_응답을_받는다() {
        final Map<String, String> signUpParams = MemberApiFixture.signUpParams1();
        MemberApiFixture.signUp(signUpParams);

        final Map<String, String> cookies = LoginApiFixture.memberLoginAndGetCookies(signUpParams);
        final Map<String, String> themeParams = ThemeApiFixture.themeParams1();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(themeParams)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }


    @Test
    void 이미_존재하는_테마_이름으로_테마_생성을_요청하면_CONFLICT_응답을_받는다() {
        final Map<String, String> cookies = LoginApiFixture.adminLoginAndGetCookies();
        final Map<String, String> themeParams = ThemeApiFixture.themeParams1();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(themeParams)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(themeParams)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void 권리자_권한으로_테마를_삭제한다() {
        final Map<String, String> cookies = LoginApiFixture.adminLoginAndGetCookies();

        final Map<String, String> themeParams = ThemeApiFixture.themeParams1();
        final Integer themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(themeParams)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().delete("/themes/{id}", themeId)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }


    @Test
    void 존재하지_않는_테마를_삭제하면_예외가_발생한다() {
        final Map<String, String> cookies = LoginApiFixture.adminLoginAndGetCookies();

        final Integer themeId = Integer.MAX_VALUE;
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().delete("/themes/{id}", themeId)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 테마를_조회한다() {
        final Map<String, String> cookies = LoginApiFixture.adminLoginAndGetCookies();

        final int sizeBeforeCreate = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().path("size()");

        final Map<String, String> themeParams = ThemeApiFixture.themeParams1();
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(themeParams)
                .when().post("/themes")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        final int sizeAfterCreate = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("size()");

        Assertions.assertThat(sizeAfterCreate)
                .isEqualTo(sizeBeforeCreate + 1);
    }
}
