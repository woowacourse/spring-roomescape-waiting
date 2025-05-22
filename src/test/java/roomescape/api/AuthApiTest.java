package roomescape.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.infrastructure.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthApiTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 로그인_성공() {
        //given
        memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.MEMBER).build()
        );
        Map<String, Object> body = new HashMap<>();
        body.put("email", "email1@domain.com");
        body.put("password", "password1");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("api/auth/login")
                .then()
                .statusCode(200)
                .cookie("token", notNullValue());
    }

    @Test
    void 로그인_비밀번호_불일치로_실패() {
        //given
        memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.MEMBER).build()
        );
        Map<String, Object> body = new HashMap<>();
        body.put("email", "email1@domain.com");
        body.put("password", "password2");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/auth/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("이메일 또는 비밀번호를 확인해주세요."));
    }

    @Test
    void 로그인_존재하지_않는_이메일로_실패() {
        //given
        Map<String, Object> body = new HashMap<>();
        body.put("email", "email1@domain.com");
        body.put("password", "password2");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/auth/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("이메일 또는 비밀번호를 확인해주세요."));
    }

    @Test
    void 인증_정보_조회_성공() {
        //given
        memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.MEMBER).build()
        );
        Map<String, Object> body = Map.of(
                "email", "email1@domain.com",
                "password", "password1"
        );
        String token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("api/auth/check")
                .then()
                .statusCode(200)
                .body("name", equalTo("member1"));
    }

    @Test
    void 잘못된_토큰으로_인증_정보_조회_시_401에러가_발생한다() {
        //given
        memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.MEMBER).build()
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", "thisIsInvalidToken")
                .when().get("api/auth/check")
                .then()
                .statusCode(401);
    }

}