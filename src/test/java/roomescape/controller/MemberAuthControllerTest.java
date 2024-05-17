package roomescape.controller;

import static roomescape.Fixture.COOKIE_NAME;
import static roomescape.Fixture.VALID_USER_EMAIL;
import static roomescape.Fixture.VALID_USER_NAME;
import static roomescape.Fixture.VALID_USER_PASSWORD;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.MemberRole;
import roomescape.web.controller.request.MemberSignUpWebRequest;
import roomescape.web.controller.request.TokenWebRequest;

class MemberAuthControllerTest extends ControllerTest {

    @BeforeEach
    void setInitialData() {
        jdbcTemplate.update(
            "INSERT INTO member(name,email,password,role) VALUES (?,?,?,?)",
            VALID_USER_NAME.getName(),
            VALID_USER_EMAIL.getEmail(),
            VALID_USER_PASSWORD.getPassword(),
            MemberRole.USER.name());
    }

    @DisplayName("로그인을 한다. -> 200")
    @Test
    void login() {
        TokenWebRequest request = new TokenWebRequest(VALID_USER_EMAIL.getEmail(),
            VALID_USER_PASSWORD.getPassword());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/login")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("존재하지 않는 회원으로 로그인을 한다. -> 401")
    @Test
    void login_NotMemberEmail() {
        TokenWebRequest request = new TokenWebRequest("회원아님@naver.com",
            VALID_USER_PASSWORD.getPassword());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/login")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("올바르지 않는 이메일 형식으로 로그인을 한다. -> 400")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"이메일"})
    void login_IllegalEmail(String illegalEmail) {
        TokenWebRequest request = new TokenWebRequest(illegalEmail,
            VALID_USER_PASSWORD.getPassword());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/login")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("올바르지 않는 비밀 번호로 로그인을 한다. -> 400")
    @ParameterizedTest
    @NullAndEmptySource
    void login_IllegalPassword(String illegalPassword) {
        TokenWebRequest request = new TokenWebRequest(VALID_USER_EMAIL.getEmail(),
            illegalPassword);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/login")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("로그인 되어 있는 회원을 조회한다. -> 200")
    @Test
    void findLoginMember() {
        RestAssured.given().log().all()
            .cookie(COOKIE_NAME, getUserToken())
            .when().get("/login/check")
            .then().log().all()
            .statusCode(200);
    }

    @DisplayName("로그인 되어 있지 않은 회원을 조회한다. -> 400")
    @Test
    void findNotLoginMember() {
        RestAssured.given().log().all()
            .when().get("/login/check")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("토큰이 만료된 회원을 조회한다. -> 401")
    @Test
    void findMember_expiredToken() {
        RestAssured.given().log().all()
            .cookie(COOKIE_NAME, VALID_USER_EMAIL.getEmail())
            .when().get("/login/check")
            .then().log().all()
            .statusCode(401);
    }

    @DisplayName("회원 가입을 한다. -> 201")
    @Test
    void signUp() {
        MemberSignUpWebRequest request = new MemberSignUpWebRequest("스티치",
            "test@gmail.com", "1234");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/signup")
            .then().log().all()
            .statusCode(201);
    }

    @DisplayName("이미 존재하는 이메일로 회원 가입을 한다. -> 401")
    @Test
    void signUp_ExistsEmail() {
        MemberSignUpWebRequest request = new MemberSignUpWebRequest(VALID_USER_NAME.getName(),
            VALID_USER_EMAIL.getEmail(), VALID_USER_PASSWORD.getPassword());

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/signup")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("잘못된 형식의 이름으로 회원 가입을 한다. -> 400")
    @ParameterizedTest
    @NullAndEmptySource
    void signUp_IllegalName(String illegalName) {
        MemberSignUpWebRequest request = new MemberSignUpWebRequest(illegalName,
            "test@gmail.com", "1234");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/signup")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("잘못된 형식의 이메일로 회원 가입을 한다. -> 400")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"이메일"})
    void signUp_IllegalEmail(String illegalEmail) {
        MemberSignUpWebRequest request = new MemberSignUpWebRequest("스티치",
            illegalEmail, "1234");

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/signup")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("잘못된 형식의 비밀번호로 회원 가입을 한다. -> 400")
    @ParameterizedTest
    @NullAndEmptySource
    void signUp_IllegalPW(String illegalPassword) {
        MemberSignUpWebRequest request = new MemberSignUpWebRequest("스티치",
            "test@gmail.com", illegalPassword);

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/signup")
            .then().log().all()
            .statusCode(400);
    }

    @DisplayName("로그아웃 한다. -> 200")
    @Test
    void logout() {
        RestAssured.given().log().all()
            .cookie(COOKIE_NAME, getUserToken())
            .when().post("/logout")
            .then().log().all()
            .statusCode(200);
    }
}