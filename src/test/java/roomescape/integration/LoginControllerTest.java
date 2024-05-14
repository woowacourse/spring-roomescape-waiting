package roomescape.integration;

import static org.hamcrest.Matchers.is;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.WRONG_PASSWORD;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.Fixture;
import roomescape.domain.Member;
import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class LoginControllerTest {

    @LocalServerPort
    int port;
    private Member defaultUser = Fixture.defaultMember;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void init() {
        RestAssured.port = port;
        defaultUser = memberRepository.save(defaultUser);
        System.out.println(defaultUser);
    }

    @DisplayName("올바른 로그인 요청에 대해 토큰 값을 가진 쿠키가 생성된다.")
    @Test
    void loginTest() {
        RestAssured.given().log().all()
                .when().body(Map.of(
                        "email", defaultUser.getEmail(),
                        "password", defaultUser.getPassword()
                ))
                .contentType(ContentType.JSON)
                .post("/login")
                .then().log().all()
                .statusCode(200)
                .cookie("token");
    }

    @DisplayName("존재하지 않는 Email 로 요청을 보내면 예외가 발생한다.")
    @Test
    void notFoundUserLoginTest() {
        RestAssured.given().log().all()
                .when()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "wrongEmail",
                        "password", defaultUser.getPassword()
                )).post("/login")
                .then().log().all()
                .statusCode(400)
                .body("message", is(NOT_FOUND_MEMBER.getMessage()));
    }

    @DisplayName("잘못된 비밀번호로 요청을 보내면 예외가 발생한다.")
    @Test
    void wrongPasswordLoginTest() {
        RestAssured.given().log().all()
                .when()
                .contentType(ContentType.JSON
                ).body(Map.of(
                        "email", defaultUser.getEmail(),
                        "password", "wrongPassword"
                )).post("/login")
                .then().log().all()
                .statusCode(400)
                .body("message", is(WRONG_PASSWORD.getMessage()));
    }

    @DisplayName("로그인을 해 토큰이 생성된 경우")
    @Nested
    class WhenLogin {
        private String token;

        @BeforeEach
        void getToken() {
            token = RestAssured.given().log().all()
                    .when().body(Map.of(
                            "email", defaultUser.getEmail(),
                            "password", defaultUser.getPassword()
                    ))
                    .contentType(ContentType.JSON)
                    .post("/login")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .cookie("token");
        }

        @DisplayName("토큰을 이용해 로그인된 유저의 이름을 볼 수 있다.")
        @Test
        void loginCheckTest() {
            RestAssured.given().log().all()
                    .cookie("token", token)
                    .get("/login/check")
                    .then().log().all()
                    .statusCode(200)
                    .body("name", is(defaultUser.getName()));
        }
    }
}
