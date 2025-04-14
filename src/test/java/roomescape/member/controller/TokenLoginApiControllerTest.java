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
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.service.MemberService;

class TokenLoginApiControllerTest extends BaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    MemberService memberService;

    Map<String, String> member;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        member = new HashMap<>();
        memberService.save(new SignUpRequest("매트", "matt.kakao", "1234"));
        member.put("name", "matt");
        member.put("email", "matt.kakao");
        member.put("password", "1234");
    }

    @Test
    @DisplayName("토큰 로그인에 성공한다.")
    void tokenLogin() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(member)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("회원가입되지 않은 회원은 로그인에 실패한다.")
    void tokenLoginFailTest() {
        Map<String, String> failMap = new HashMap<>(
                Map.of(
                        "name", "말론", "email", "malone.gmail", "password", "1234"
                )
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(failMap)
                .when().post("/login")
                .then().log().all()
                .statusCode(400);
    }
}
