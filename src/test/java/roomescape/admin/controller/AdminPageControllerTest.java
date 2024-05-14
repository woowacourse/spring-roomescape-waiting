package roomescape.admin.controller;

import static roomescape.InitialMemberFixture.COMMON_PASSWORD;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_1;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_4;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class AdminPageControllerTest {

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/time", "/admin/theme", "/admin/reservation"})
    @DisplayName("토큰 없이 /admin 페이지에 접근하면 예외가 발생한다.")
    void throwExceptionWithoutToken(String url) {
        RestAssured.given().log().all()
                .when().get(url)
                .then().log().all()
                .statusCode(401);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/time", "/admin/theme", "/admin/reservation"})
    @DisplayName("관리자 권한이 없는 토큰으로 /admin 페이지에 접근하면 예외가 발생한다.")
    void throwExceptionIfNotAdmin(String url) {
        Map<String, String> memberParam = new HashMap<>();
        memberParam.put("password", COMMON_PASSWORD.password());
        memberParam.put("email", LOGIN_MEMBER_1.getEmail().email());

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParam)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get(url)
                .then().log().all()
                .statusCode(401);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/time", "/admin/theme", "/admin/reservation"})
    @DisplayName("관리자 권한이 있는 토큰으로 /admin 페이지에 접근하면 접근이 가능하다.")
    void AdminCanAccessAdminPage(String url) {
        Map<String, String> memberParam = new HashMap<>();
        memberParam.put("password", COMMON_PASSWORD.password());
        memberParam.put("email", LOGIN_MEMBER_4.getEmail().email());

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParam)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get(url)
                .then().log().all()
                .statusCode(200);
    }
}
