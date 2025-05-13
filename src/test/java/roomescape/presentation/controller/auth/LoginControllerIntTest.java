package roomescape.presentation.controller.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.testFixture.Fixture.resetH2TableIds;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.application.auth.dto.LoginRequest;
import roomescape.application.auth.dto.LoginResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginControllerIntTest {

    @LocalServerPort
    int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        RestAssured.port = port;
        resetH2TableIds(jdbcTemplate);
    }

    @DisplayName("토큰으로 로그인 성공")
    @Test
    void tokenLogin() {
        jdbcTemplate.update("INSERT INTO member (name, email, password) VALUES ('어드민', 'admin@email.com', 'password')");

        String cookie = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin@email.com", "password"))
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().header("Set-Cookie").split(";")[0].substring("token=".length());

        LoginResponse member = RestAssured
                .given().log().all()
                .cookie("token", cookie)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value()).extract().as(LoginResponse.class);

        assertThat(member.name()).isEqualTo("어드민");
    }
}
