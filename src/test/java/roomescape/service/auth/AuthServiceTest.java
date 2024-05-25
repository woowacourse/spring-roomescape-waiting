package roomescape.service.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.exception.NotFoundException;
import roomescape.service.dto.request.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("존재하지 않는 아이디, 비밀번호로 로그인시 예외가 발생한다.")
    @Test
    void login() {
        // given
        String id = "not_exist_id@email.com";
        String password = "not_exist_password";

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginRequest(id, password)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("일치하는 회원 정보가 없습니다. email = " + id + ", password = " + password);
    }
}
