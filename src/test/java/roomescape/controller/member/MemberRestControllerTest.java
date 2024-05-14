package roomescape.controller.member;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.global.JwtManager;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.service.dto.member.MemberCreateRequest;
import roomescape.service.dto.member.MemberLoginRequest;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MemberRestControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtManager jwtManager;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

    }

    @DisplayName("회원가입에 성공하면 응답과 201 상태코드를 반환한다.")
    @Test
    void return_201_when_signup() {
        MemberCreateRequest createRequest = new MemberCreateRequest("t1@t1.com", "123", "재즈");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when().post("/members/signup")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("회원가입에 실패하면 400 상태코드를 반환한다.")
    @Test
    void return_400_when_fail_signup() {
        MemberCreateRequest createRequest = new MemberCreateRequest("t1@t1.com", "", "재즈");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when().post("/members/signup")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("로그인에 성공하면 200 상태코드와 토큰을 반환한다.")
    @Test
    void return_201_and_token_when_login() {
        MemberCreateRequest createRequest = new MemberCreateRequest("t1@t1.com", "123", "재즈");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when().post("/members/signup")
                .then().log().all()
                .statusCode(201);

        MemberLoginRequest loginRequest = new MemberLoginRequest("t1@t1.com", "123");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/members/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        assertThat(token).isNotNull();
    }

    @DisplayName("로그인에 실패하면 400 상태코드를 반환한다.")
    @Test
    void return_400_when_fail_login() {
        MemberCreateRequest createRequest = new MemberCreateRequest("t1@t1.com", "123", "재즈");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when().post("/members/signup")
                .then().log().all()
                .statusCode(201);

        MemberLoginRequest loginRequest = new MemberLoginRequest("t1@t1.com", "1234");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/members/login")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("유효한 토큰인지 검증되면 200 상태코드와 로그인된 사용자의 정보를 응답한다.")
    @Test
    void return_200_when_login_check() {
        MemberCreateRequest createRequest = new MemberCreateRequest("t1@t1.com", "123", "재즈");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when().post("/members/signup")
                .then().log().all()
                .statusCode(201);

        MemberLoginRequest loginRequest = new MemberLoginRequest("t1@t1.com", "123");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/members/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/members/login/check")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("유효한 토큰이 아니면 401 상태코드를 응답한다.")
    @Test
    void return_401_when_fail_login_check() {
        MemberCreateRequest createRequest = new MemberCreateRequest("t1@t1.com", "123", "재즈");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when().post("/members/signup")
                .then().log().all()
                .statusCode(201);

        MemberLoginRequest loginRequest = new MemberLoginRequest("t1@t1.com", "123");
        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/members/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", "token" + "zxczdasd")
                .when().get("/members/login/check")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("로그아웃 시 토큰을 만료시키고 200 상태코드를 반환한다.")
    @Test
    void return_200_when_logout() {
        MemberCreateRequest createRequest = new MemberCreateRequest("t1@t1.com", "123", "재즈");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when().post("/members/signup")
                .then().log().all()
                .statusCode(201);

        MemberLoginRequest loginRequest = new MemberLoginRequest("t1@t1.com", "123");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/members/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        String token = RestAssured.given().log().all()
                .when().post("/members/logout")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        assertThat(token).isEmpty();
    }

    @DisplayName("멤버 목록을 조회하는데 성공하면 응답과 200 상태코드를 반환한다.")
    @Test
    void return_200_when_find_all_members() {
        Member admin = new Member(2L, "t2@t2.com", "124", "재즈", "ADMIN");
        String adminToken = jwtManager.generateToken(admin);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("로그인하지 않은 유저가 멤버 목록을 조회할 시 로그인 페이지로 리다이렉트 시킨다.")
    @Test
    void return_302_when_not_login_member_find_all_members() {
        RestAssured.given().log().all()
                .redirects().follow(false)
                .contentType(ContentType.JSON)
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(302)
                .header("Location", "http://localhost:" + port + "/login");
    }

    @DisplayName("어드민이 아닌 멤버가 멤버 목록을 조회할 시 로그인 페이지로 리다이렉트 시킨다.")
    @Test
    void return_302_when_not_admin_find_all_members() {
        Member member = new Member(2L, "t2@t2.com", "124", "재즈", "MEMBER");
        String memberToken = jwtManager.generateToken(member);

        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("token", memberToken)
                .contentType(ContentType.JSON)
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(302)
                .header("Location", "http://localhost:" + port + "/");
    }
}
