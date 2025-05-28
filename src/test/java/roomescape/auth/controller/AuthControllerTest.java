package roomescape.auth.controller;

import io.jsonwebtoken.Claims;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.auth.domain.dto.TokenRequestDto;
import roomescape.global.auth.domain.dto.TokenResponseDto;
import roomescape.auth.fixture.AuthFixture;
import roomescape.global.auth.service.AuthService;
import roomescape.user.domain.Role;
import roomescape.user.domain.User;
import roomescape.user.fixture.UserFixture;
import roomescape.user.repository.UserRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    private static final String TOKEN_NAME_FILED = "token";

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserRepository userRepository;

    private User savedMember;
    private User savedAdmin;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        User member = UserFixture.create(Role.ROLE_MEMBER, "actlls1n", "actlls1e", "actlls1p");
        savedMember = userRepository.save(member);

        User admin = UserFixture.create(Role.ROLE_ADMIN, "actlls2n", "actlls2e", "actlls2p");
        savedAdmin = userRepository.save(admin);
    }

    @Nested
    @DisplayName("로그인 기능")
    class login {

        @DisplayName("유효한 이메일과 비밀번호로 로그인을 성공한다. : member")
        @Test
        void login_success_byValidMember() {
            // given

            // when
            TokenRequestDto requestDto = new TokenRequestDto(savedMember.getEmail(), savedMember.getPassword());

            // then
            RestAssured
                    .given().log().all()
                    .contentType(ContentType.JSON)
                    .body(requestDto)
                    .when().post("/signin")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract().response();
        }

        @DisplayName("유효한 이메일과 비밀번호로 로그인을 성공한다. : admin")
        @Test
        void login_success_byValidAdmin() {
            // given

            // when
            TokenRequestDto requestDto = new TokenRequestDto(savedAdmin.getEmail(), savedAdmin.getPassword());

            // then
            RestAssured
                    .given().log().all()
                    .contentType(ContentType.JSON)
                    .body(requestDto)
                    .when().post("/signin")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .extract().response();
        }

        @DisplayName("유효하지 않는 이메일과 비밀번호로 로그인을 시도하여 실패: 상태 코드 404를 반환한다.")
        @Test
        void login_throwException_byInvalidEmail() {
            // given
            // when
            TokenRequestDto requestDto = new TokenRequestDto("invalidEmail@example.com", "adfasdf");

            // then
            RestAssured
                    .given().log().all()
                    .contentType(ContentType.JSON)
                    .body(requestDto)
                    .when().post("/signin")
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .extract().response();
        }
    }

    @Nested
    @DisplayName("토큰 기반 유저 조회 API 테스트")
    class checkAuth {

        @DisplayName("ROLE_MEMBER 토큰으로 유저 조회 시 200 OK 반환")
        @Test
        void checkAuth_success_withRoleISMember() {
            // given
            TokenRequestDto requestDto = AuthFixture.createTokenRequestDto(savedMember.getEmail(),
                    savedMember.getPassword());
            TokenResponseDto responseDto = authService.login(requestDto);
            String token = responseDto.accessToken();

            // when
            // then
            RestAssured
                    .given().log().all()
                    .cookie(TOKEN_NAME_FILED, token)
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value());

            Claims claims = jwtTokenProvider.getClaims(token);
            String actualRoleName = claims.get("role", String.class);

            Assertions.assertThat(actualRoleName).isEqualTo(Role.ROLE_MEMBER.name());
        }

        @DisplayName("ROLE_ADMIN 토큰으로 유저 조회 시 200 OK 반환")
        @Test
        void checkAuth_success_withRoleISAdmin() {
            // given
            TokenRequestDto requestDto = AuthFixture.createTokenRequestDto(savedAdmin.getEmail(),
                    savedAdmin.getPassword());
            TokenResponseDto responseDto = authService.login(requestDto);

            // when
            String token = responseDto.accessToken();

            // then
            RestAssured
                    .given().log().all()
                    .cookie(TOKEN_NAME_FILED, token)
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value());

            Claims claims = jwtTokenProvider.getClaims(token);
            String actualRoleName = claims.get("role", String.class);

            Assertions.assertThat(actualRoleName).isEqualTo(Role.ROLE_ADMIN.name());
        }
    }
}
