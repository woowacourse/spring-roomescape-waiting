package roomescape.auth;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.util.IntegrationTest;

@IntegrationTest
class AuthIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    @Test
    @DisplayName("로그인 성공: Set-Cookie 헤더에 쿠키 값 전달")
    void login() {
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@naver.com");
        params.put("password", "hihi");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("로그인 실패: 로그인 계정 없음")
    void login_WhenMemberNotExist() {
        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@naver.com");
        params.put("password", "hihi");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(404)
                .body("detail", equalTo("로그인하려는 계정이 존재하지 않습니다. 회원가입 후 로그인해주세요."));
    }

    @Test
    @DisplayName("로그인 실패: 이메일에 null")
    void login_WhenEmailIsNull() {
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("email", null);
        params.put("password", "hihi");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("detail", equalTo("이메일은 공백 문자가 불가능합니다."));
    }

    @Test
    @DisplayName("로그인 실패: 이메일 형식")
    void login_WhenEmailIsInvalidType() {
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("email", "nulasdfl");
        params.put("password", "hihi");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("detail", equalTo("이메일은 메일 형식만 가능합니다."));
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호 오류")
    void login_WhenPasswordNotCorrect() {
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@naver.com");
        params.put("password", "hihi123");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("detail", equalTo("아이디 또는 비밀번호를 잘못 입력했습니다. 다시 입력해주세요."));
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호 null")
    void login_WhenPasswordIsNull() {
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@naver.com");
        params.put("password", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(400)
                .body("detail", equalTo("비밀번호는 공백 문자가 불가능합니다."));
    }

    @Test
    @DisplayName("로그인한 회원의 정보 조회 성공")
    void loginCheck() {
        // give
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@naver.com");
        params.put("password", "hihi");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .extract().cookie("token");

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .body("name", equalTo("몰리"));
    }

    @Test
    @DisplayName("로그인한 회원의 정보 조회 실패: 쿠키 없음")
    void loginCheck_WhenCookieNotExist() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .body("detail", equalTo("쿠키가 없어서 회원 정보를 찾을 수 없습니다. 다시 로그인해주세요."));
    }

    @Test
    @DisplayName("로그인한 회원의 정보 조회 실패: 토큰 쿠키 없음")
    void loginCheck_WhenTokenNotExist() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("tokenToken", null)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401)
                .body("detail", equalTo("토큰에 대한 쿠키가 없어서 회원 정보를 찾을 수 없습니다. 다시 로그인해주세요."));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout() {
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("email", "login@naver.com");
        params.put("password", "hihi");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().post("/logout")
                .then().log().all()
                .statusCode(204);
    }
}

