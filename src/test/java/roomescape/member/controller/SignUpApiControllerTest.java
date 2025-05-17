package roomescape.member.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.common.BaseTest;
import roomescape.member.service.MemberService;

class SignUpApiControllerTest extends BaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    MemberService memberService;

    private Map<String, String> member;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        member = new HashMap<>();
        member.put("name", "matt");
        member.put("email", "matt@kakao.com");
        member.put("password", "1234");
    }

    @Test
    @DisplayName("회원 가입을 진행한다.")
    void signUpTest() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(member)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("이름이 공백이면 회원 가입에 실패한다.")
    void nameNullSignUpTest() {
        Map<String, String> failMap = new HashMap<>(
                Map.of(
                        "name", "", "email", "matt.kakao", "password", "1234"
                )
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(failMap)
                .when().post("/members")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("이메일이 공백이면 회원 가입에 실패한다.")
    void emailNullSignUpTest() {
        Map<String, String> failMap = new HashMap<>(
                Map.of(
                        "name", "matt", "email", "", "password", "1234"
                )
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(failMap)
                .when().post("/members")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("비밀번호가 공백이면 회원 가입에 실패한다.")
    void passwordNullSignUpTest() {
        Map<String, String> failMap = new HashMap<>(
                Map.of(
                        "name", "matt", "email", "matt.kakao", "password", ""
                )
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(failMap)
                .when().post("/members")
                .then().log().all()
                .statusCode(400);
    }
}
