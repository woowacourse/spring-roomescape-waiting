package roomescape.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_BROWN;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.LoginResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class LoginControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("로그인을 하고, 해당 정보를 받아올 수 있다.")
    @Test
    void loginTest() {
        LoginRequest request = new LoginRequest(MEMBER_BROWN.getEmail(), MEMBER_BROWN.getPassword());
        LoginResponse expectedResponse = new LoginResponse(MEMBER_BROWN.getName());

        Cookies cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().detailedCookies();
        LoginResponse response = RestAssured.given().log().all()
                .cookies(cookies)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getObject(".", LoginResponse.class);

        assertThat(response).isEqualTo(expectedResponse);
    }
}
