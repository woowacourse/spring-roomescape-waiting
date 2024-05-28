package roomescape.controller.api;

import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.controller.BaseControllerTest;
import roomescape.service.dto.request.LoginRequest;
import roomescape.service.dto.response.member.MemberIdAndNameResponse;
import roomescape.util.TokenGenerator;

class AuthApiControllerTest extends BaseControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        jdbcTemplate.update(
                "INSERT INTO member (name, email, password, `role`) VALUES ('사용자1', 'user@wooteco.com', '1234', 'USER')");
    }

    @Test
    @DisplayName("올바른 로그인 정보를 입력할 시, 로그인에 성공한다.")
    void authenticatedMemberLogin_Success() {
        RestAssured.given().log().all()
                .body(new LoginRequest("user@wooteco.com", "1234"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("잘못된 로그인 정보를 입력할 시, 로그인에 실패한다.")
    void authenticatedMemberLogin_Failure() {
        RestAssured.given().log().all()
                .body(new LoginRequest("wrong@wooteco.com", "1234"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("로그인한 사용자의 인증 정보 조회 시, 성공한다.")
    void authenticatedMemberLoginCheck_Success() {
        MemberIdAndNameResponse response = RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeUserToken())
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200).extract().as(MemberIdAndNameResponse.class);

        Assertions.assertThat(response.name()).isEqualTo("사용자1");
    }

    @Test
    @DisplayName("로그인하지 않은 사용자의 인증 정보 조회 시, 실패한다.")
    void authenticatedMemberLoginCheck_Failure() {
        RestAssured.given().log().all()
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401);
    }
}
