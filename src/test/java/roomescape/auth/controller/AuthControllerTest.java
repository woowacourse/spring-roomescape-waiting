package roomescape.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.Role;
import roomescape.util.ControllerTest;

@DisplayName("회원 API 통합 테스트")
class AuthControllerTest extends ControllerTest {
    @Autowired
    AuthService authService;

    @DisplayName("로그인 페이지 조회 시, 200을 반환한다.")
    @Test
    void getLoginPage() {
        //given & when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/login")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("로그인 시, 200을 반환한다.")
    @Test
    void login() {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("email", "dev.chocochip@gmail.com");
        params.put("password", "1234");

        //when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @DisplayName("로그인 후, 토큰으로 회원을 검증한다.")
    @TestFactory
    Stream<DynamicTest> loginAndCheck() {
        List<String> tokens = new ArrayList<>();
        List<AuthInfo> authInfos = new ArrayList<>();
        String email = "dev.chocochip@gmail.com";

        return Stream.of(
                dynamicTest("로그인에 성공할 경우, 200을 반환한다.", () -> {
                    //given
                    String password = "1234";

                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);

                    //when & then
                    String token = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(params)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");

                    tokens.add(token);
                }),
                dynamicTest("토큰을 통해 회원을 검증할 경우, 200을 반환한다.", () -> {
                    //given
                    String token = tokens.get(0);

                    //when & then
                    AuthInfo authInfo = RestAssured
                            .given().log().all()
                            .cookie("token", token)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .when().get("/login/check")
                            .then().log().all()
                            .statusCode(HttpStatus.OK.value()).extract().as(AuthInfo.class);

                    authInfos.add(authInfo);
                }),
                dynamicTest("토큰으로 얻은 회원 정보를 검증한다.", () -> {
                    //when & then
                    assertAll(
                            () -> assertThat(authInfos).hasSize(1),
                            () -> assertThat(authInfos.get(0).getId()).isEqualTo(1),
                            () -> assertThat(authInfos.get(0).getName()).isEqualTo("초코칩"),
                            () -> assertThat(authInfos.get(0).getRole()).isEqualTo(Role.USER),
                            () -> assertThat(authInfos.get(0).getEmail()).isEqualTo(email)
                    );
                })
        );
    }

    @DisplayName("로그인 후, 로그아웃에 성공한다.")
    @TestFactory
    Stream<DynamicTest> loginAndLogout() {
        List<String> tokens = new ArrayList<>();
        List<String> cookies = new ArrayList<>();
        String email = "dev.chocochip@gmail.com";

        return Stream.of(
                dynamicTest("로그인에 성공할 경우, 200을 반환한다.", () -> {
                    //given
                    String password = "1234";

                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);

                    //when & then
                    String token = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(params)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .when().post("/login")
                            .then().log().all()
                            .statusCode(HttpStatus.OK.value()).extract().cookie("token");
                    tokens.add(token);
                }),
                dynamicTest("토큰을 통해 로그아웃할 경우, 200을 반환한다.", () -> {
                    //given
                    String token = tokens.get(0);

                    //when & then
                    String cookie = RestAssured
                            .given().log().all()
                            .cookie("token", token)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .when().post("/logout")
                            .then().log().all()
                            .statusCode(HttpStatus.OK.value()).extract().cookie("token");

                    cookies.add(cookie);
                }),
                dynamicTest("로그아웃한 토큰을 만료된다.", () -> {
                    //when & then
                    assertAll(
                            () -> assertThat(cookies).hasSize(1),
                            () -> assertThat(cookies.get(0)).isBlank()
                    );
                })
        );
    }

    @DisplayName("회원 가입 시, 201을 반환한다.")
    @Test
    void signup() {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("name", "chocochip2");
        params.put("email", "dev.chocochip2@gmail.com");
        params.put("password", "12345");

        //when & then
        RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .body(params)
                .when().post("/signup")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("회원 가입 후, 중복된 메일로 회원 가입할 경우 예외가 발생한다.")
    @TestFactory
    Stream<DynamicTest> duplicatedEmail() {
        String email = "dev.chocochip2@gmail.com";

        return Stream.of(
                dynamicTest("회원가입에 성공할 경우, 201을 반환한다.", () -> {
                    //given
                    Map<String, String> params = new HashMap<>();
                    params.put("name", "chocochip3");
                    params.put("email", email);
                    params.put("password", "1234");

                    //when & then
                    RestAssured
                            .given().log().all()
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .body(params)
                            .when().post("/signup")
                            .then().log().all()
                            .statusCode(HttpStatus.CREATED.value());
                }),
                dynamicTest("중복된 메일로 회원 가입할 경우, 409를 반환한다.", () -> {
                    //given
                    Map<String, String> params = new HashMap<>();
                    params.put("name", "chocchocchop");
                    params.put("email", email);
                    params.put("password", "1234");

                    RestAssured
                            .given().log().all()
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .body(params)
                            .when().post("/signup")
                            .then().log().all()
                            .statusCode(HttpStatus.CONFLICT.value());
                })
        );
    }

    @DisplayName("메일 형식이 아닐 경우, 400을 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "chocochip", "chocochip@"})
    void signupInvalidMailFormat(String invalidMail) {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("name", "chocochip2");
        params.put("email", invalidMail);
        params.put("password", "12345");

        //when & then
        RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .body(params)
                .when().post("/signup")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
