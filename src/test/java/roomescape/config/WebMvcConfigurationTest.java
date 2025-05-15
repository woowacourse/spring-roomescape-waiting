package roomescape.config;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import roomescape.global.auth.domain.dto.TokenRequestDto;
import roomescape.global.auth.domain.dto.TokenResponseDto;
import roomescape.auth.fixture.AuthFixture;
import roomescape.global.auth.service.AuthService;
import roomescape.user.AdminTestDataConfig;
import roomescape.user.MemberTestDataConfig;
import roomescape.user.domain.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {MemberTestDataConfig.class, AdminTestDataConfig.class})
class WebMvcConfigurationTest {

    private static final String TOKEN_NAME_FILED = "token";

    @Autowired
    private MemberTestDataConfig memberTestDataConfig;
    @Autowired
    private AdminTestDataConfig adminTestDataConfig;
    @Autowired
    private AuthService authService;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("/admin/** URL 요청 시 인터셉터 발동")
    class addInterceptors {

        private final User member = memberTestDataConfig.getSavedUser();
        private final User admin = adminTestDataConfig.getSavedAdmin();

        @DisplayName("admin 권한을 가지고 있는 관리자가 /admin/** URL로 요청 시 가로채지지 않는다.")
        @Test
        void addInterceptors_pass_withAdminRole() {
            // given
            TokenRequestDto requestDto = AuthFixture.createTokenRequestDto(admin.getEmail(), admin.getPassword());
            TokenResponseDto responseDto = authService.login(requestDto);
            String token = responseDto.accessToken();

            // when
            // then
            RestAssured
                    .given().log().all()
                    .cookie(TOKEN_NAME_FILED, token)
                    .when().get("/admin")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value());
        }

        @DisplayName("토큰이 없이 /admin/** URl로 요청 시 NotFoundCookieException 예외 발생한다.")
        @Test
        void addInterceptors_throwException_byNonExistToken() {
            // given
            // when
            // then
            RestAssured
                    .given().log().all()
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @DisplayName("admin 권한을 가지고 있지 않는 유저가 /admin/** URL로 요청 시 false 반환 : member 권한")
        @Test
        void addInterceptors_false_byRoleIsNotAdmin() {
            // given
            TokenRequestDto requestDto = AuthFixture.createTokenRequestDto(member.getEmail(), member.getPassword());
            TokenResponseDto responseDto = authService.login(requestDto);
            String token = responseDto.accessToken();

            // when
            // then
            RestAssured
                    .given().log().all()
                    .cookie(TOKEN_NAME_FILED, token)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }
    }
}
