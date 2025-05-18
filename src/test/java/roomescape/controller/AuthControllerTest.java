//package roomescape.controller;
//
//import io.jsonwebtoken.Claims;
//import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
//import org.springframework.http.HttpStatus;
//import org.springframework.test.annotation.DirtiesContext;
//import roomescape.domain.Role;
//import roomescape.dto.request.LoginRequest;
//import roomescape.dto.response.AccessTokenResponse;
//import roomescape.repository.UserRepository;
//import roomescape.service.AuthService;
//import roomescape.utility.JwtTokenProvider;
//
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//class AuthControllerTest {
//
//    private static final String TOKEN_NAME_FILED = "token";
//
//    @Autowired
//    private AuthService authService;
//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;
//    @Autowired
//    private UserRepository userRepository;
//
//    @Nested
//    @DisplayName("로그인 할 수 있다.")
//    class login {
//
//        @DisplayName("이메일과 비밀번호를 통해 로그인할 수 있다.")
//        @Test
//        void canLogin() {
//            // given
//
//            // when
//            LoginRequest requestDto = new LoginRequest(savedMember.getEmail(), savedMember.getPassword());
//
//            // then
//            RestAssured
//                    .given().log().all()
//                    .contentType(ContentType.JSON)
//                    .body(requestDto)
//                    .when().post("/login")
//                    .then().log().all()
//                    .statusCode(HttpStatus.OK.value())
//                    .extract().response();
//        }
//
//        @DisplayName("이메일에 해당하는 계정이 존재하지 않는 경우 로그인할 수 없다.")
//        @Test
//        void login_success2() {
//            // given
//
//            // when
//            LoginRequest requestDto = new LoginRequest(savedAdmin.getEmail(), savedAdmin.getPassword());
//
//            // then
//            RestAssured
//                    .given().log().all()
//                    .contentType(ContentType.JSON)
//                    .body(requestDto)
//                    .when().post("/login")
//                    .then().log().all()
//                    .statusCode(HttpStatus.OK.value())
//                    .extract().response();
//        }
//
//        @DisplayName("비밀번호가 맞지 않는 경우 로그인할 수 없다.")
//        @Test
//        void login_throwException_byInvalidEmail() {
//            // given
//            // when
//            LoginRequest requestDto = new LoginRequest("invalidEmail@example.com", "adfasdf");
//
//            // then
//            RestAssured
//                    .given().log().all()
//                    .contentType(ContentType.JSON)
//                    .body(requestDto)
//                    .when().post("/login")
//                    .then().log().all()
//                    .statusCode(HttpStatus.NOT_FOUND.value())
//                    .extract().response();
//        }
//    }
//
//    @Nested
//    @DisplayName("로그인을 체크할 수 있다.")
//    class checkAuth {
//
//        @DisplayName("쿠키에 담긴 토큰을 통해 로그인을 체크할 수 있다.")
//        @Test
//        void checkAuth_success_withRoleISMember() {
//            // given
//            LoginRequest requestDto = AuthFixture.createTokenRequestDto(savedMember.getEmail(),
//                    savedMember.getPassword());
//            AccessTokenResponse responseDto = authService.login(requestDto);
//            String token = responseDto.accessToken();
//
//            // when
//            // then
//            RestAssured
//                    .given().log().all()
//                    .cookie(TOKEN_NAME_FILED, token)
//                    .when().get("/login/check")
//                    .then().log().all()
//                    .statusCode(HttpStatus.OK.value());
//
//            Claims claims = jwtTokenProvider.getClaims(token);
//            String actualRoleName = claims.get("role", String.class);
//
//            Assertions.assertThat(actualRoleName).isEqualTo(Role.ROLE_MEMBER.name());
//        }
//
//        @DisplayName("토큰이 유효하지 않은 경우 로그인을 하지 않은 것으로 간주한다.")
//        @Test
//        void checkAuth_success_withRoleISAdmin() {
//            // given
//            LoginRequest requestDto = AuthFixture.createTokenRequestDto(savedAdmin.getEmail(),
//                    savedAdmin.getPassword());
//            AccessTokenResponse responseDto = authService.login(requestDto);
//
//            // when
//            String token = responseDto.accessToken();
//
//            // then
//            RestAssured
//                    .given().log().all()
//                    .cookie(TOKEN_NAME_FILED, token)
//                    .when().get("/login/check")
//                    .then().log().all()
//                    .statusCode(HttpStatus.OK.value());
//
//            Claims claims = jwtTokenProvider.getClaims(token);
//            String actualRoleName = claims.get("role", String.class);
//
//            Assertions.assertThat(actualRoleName).isEqualTo(Role.ROLE_ADMIN.name());
//        }
//
//        @DisplayName("토큰의 유효기간이 지난 경우 로그인을 하지 않은 것으로 간주한다.")
//        @Test
//        void checkAuth_success_withRoleISAdmin() {
//            // given
//            LoginRequest requestDto = AuthFixture.createTokenRequestDto(savedAdmin.getEmail(),
//                    savedAdmin.getPassword());
//            AccessTokenResponse responseDto = authService.login(requestDto);
//
//            // when
//            String token = responseDto.accessToken();
//
//            // then
//            RestAssured
//                    .given().log().all()
//                    .cookie(TOKEN_NAME_FILED, token)
//                    .when().get("/login/check")
//                    .then().log().all()
//                    .statusCode(HttpStatus.OK.value());
//
//            Claims claims = jwtTokenProvider.getClaims(token);
//            String actualRoleName = claims.get("role", String.class);
//
//            Assertions.assertThat(actualRoleName).isEqualTo(Role.ROLE_ADMIN.name());
//        }
//    }
//
//}
