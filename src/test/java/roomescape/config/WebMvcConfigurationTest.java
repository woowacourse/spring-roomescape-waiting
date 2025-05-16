package roomescape.config;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import roomescape.auth.fixture.AuthFixture;
import roomescape.domain.Role;
import roomescape.domain.User;
import roomescape.dto.request.TokenRequestDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.service.AuthService;
import roomescape.user.fixture.UserFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebMvcConfigurationTest {

    private static final String TOKEN_NAME_FILED = "token";

    @Autowired
    private AuthService authService;

    @Autowired
    private TestEntityManager entityManager;

    @LocalServerPort
    int port;

    private User savedMember;
    private User savedAdmin;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        // Create and persist test data
        User member = UserFixture.create(Role.ROLE_MEMBER, "member_dummyName", "member_dummyEmail",
                "member_dummyPassword");
        User admin = UserFixture.create(Role.ROLE_ADMIN, "admin_dummyName", "admin_dummyEmail", "admin_dummyPassword");

        savedMember = entityManager.persist(member);
        savedAdmin = entityManager.persist(admin);

        entityManager.flush();
    }

    @Nested
    @DisplayName("/admin/** URL 요청 시 인터셉터 발동")
    class addInterceptors {

        @DisplayName("admin 권한을 가지고 있는 관리자가 /admin/** URL로 요청 시 가로채지지 않는다.")
        @Test
        void addInterceptors_pass_withAdminRole() {
            // given
            TokenRequestDto requestDto = AuthFixture.createTokenRequestDto(savedAdmin.getEmail(),
                    savedAdmin.getPassword());
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
                    .when().get("/admin")
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @DisplayName("admin 권한을 가지고 있지 않는 유저가 /admin/** URL로 요청 시 false 반환 : member 권한")
        @Test
        void addInterceptors_false_byRoleIsNotAdmin() {
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
                    .when().get("/admin")
                    .then().log().all()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }
    }
}
