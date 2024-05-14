package roomescape.controller.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.auth.dto.SignUpRequest;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:truncate-with-guests.sql"})
class AuthControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = port;
    }

    @DisplayName("로그인 성공 테스트")
    @Test
    void login() {
        //given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest("lini", "lini@email.com", "lini123"))
                .when().post("/signup")
                .then().log().all();

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("lini123", "lini@email.com"))
                .when().post("/login")
                .then().log().all()
                .assertThat().statusCode(200);
    }

    @DisplayName("로그아웃 성공 테스트")
    @Test
    void logout() {
        //given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest("lini", "lini@email.com", "lini123"))
                .when().post("/signup")
                .then().log().all();

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().post("/logout")
                .then().log().all()
                .assertThat().statusCode(200);
    }

    @DisplayName("인증 조회 성공 테스트")
    @Test
    void check() {
        //given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest("lini", "lini@email.com", "lini123"))
                .when().post("/signup")
                .then().log().all();

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("lini123", "lini@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");

        //when&then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .assertThat().statusCode(200).body("name", is("lini"));
    }

    @DisplayName("회원가입 성공 테스트")
    @Test
    void signUp() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest("lini", "lini@email.com", "lini123"))
                .when().post("/signup")
                .then().log().all()
                .assertThat().statusCode(201);
    }
}
