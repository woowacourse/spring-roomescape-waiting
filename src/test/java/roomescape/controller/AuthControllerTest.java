package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.controller.request.LoginRequest;
import roomescape.controller.response.LoginResponse;
import roomescape.service.AuthService;
import roomescape.service.dto.AuthDto;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AuthControllerTest {

    private static final AuthDto userDto = new AuthDto("treeboss@gmail.com", "treeboss123!");

    private final JdbcTemplate jdbcTemplate;
    private final AuthService authService;
    private final SimpleJdbcInsert memberInsertActor;

    @Autowired
    public AuthControllerTest(JdbcTemplate jdbcTemplate, AuthService authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
        this.memberInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        insertMember("에버", "treeboss@gmail.com", "treeboss123!", "USER");
        insertMember("우테코", "wtc@gmail.com", "wtc123!", "ADMIN");
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY");
    }

    private void insertMember(String name, String email, String password, String role) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("name", name);
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("role", role);
        memberInsertActor.execute(parameters);
    }

    @DisplayName("로그인을 성공할 경우 사용자 정보를 바탕으로 토큰을 생성하여 쿠키에 담아 반환한다.")
    @Test
    void should_return_token_through_cookie_when_login_success() {
        String expected = authService.createToken(userDto);
        String actual = RestAssured
                .given().log().all()
                .body(new LoginRequest(userDto.getEmail(), userDto.getPassword()))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/login")
                .then().log().all()
                .extract().cookie("token");
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("로그인 된 계정의 사용자 정보를 반환한다.")
    @Test
    void should_return_name_of_login_member() {
        String token = authService.createToken(userDto);
        LoginResponse body = RestAssured
                .given().log().all()
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .extract().body().as(LoginResponse.class);
        assertThat(body.getName()).isEqualTo("에버");
    }

    @DisplayName("로그아웃을 성공할 경우 토큰 쿠키를 삭제한다.")
    @Test
    void should_logout() {
        String token = authService.createToken(userDto);
        String tokenAfterLogout = RestAssured
                .given().log().all()
                .cookie("token", token)
                .when().post("/logout")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");
        assertThat(tokenAfterLogout).isEmpty();
    }
}
