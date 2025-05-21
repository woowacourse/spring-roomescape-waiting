package roomescape.presentation.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.application.AuthenticationService;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRole;
import roomescape.exception.AuthenticationException;
import roomescape.exception.AuthorizationException;

@ExtendWith(MockitoExtension.class)
class UserArgumentResolverTest {

    @Mock
    private AuthenticationService authenticationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new TestController())
                .setCustomArgumentResolvers(new UserArgumentResolver(authenticationService))
                .build();
    }

    @Test
    @DisplayName("@Authenticated 유저를 바인딩할 때 쿠키에 유효한 토큰이 있으면 바인딩된다.")
    void bindUserWhenRequestWithValidToken() throws Exception {
        var cookie = AuthenticationTokenCookie.forResponse("validToken");
        var user = new User(1L, "유저1", UserRole.USER, "email@email.com", "password");
        Mockito.when(authenticationService.getUserByToken("validToken")).thenReturn(user);

        mockMvc.perform(get("/authenticatedUser").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": 1,
                          "name": "유저1",
                          "role": "USER",
                          "email": "email@email.com",
                          "password": "password"
                        }
                        """)
                );
    }

    @Test
    @DisplayName("@Authenticated 유저를 바인딩할 때 쿠키에 토큰이 없으면 예외가 발생한다.")
    void bindUserWhenRequestWithoutToken() {
        assertThatThrownBy(() -> mockMvc.perform(get("/authenticatedUser")))
                .hasCauseInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("@Authenticated 유저를 바인딩할 때 쿠키에 유효하지 않은 토큰이 있으면 예외가 발생한다.")
    void bindUserWhenRequestWithInvalidToken() {
        var invalidTokenCookie = AuthenticationTokenCookie.forResponse("invalidToken");
        Mockito.when(authenticationService.getUserByToken("invalidToken"))
                .thenThrow(new AuthorizationException("invalid token"));
        assertThatThrownBy(() -> mockMvc.perform(get("/authenticatedUser").cookie(invalidTokenCookie)))
                .hasCauseInstanceOf(AuthorizationException.class);
    }

    @Controller
    private static class TestController {

        @GetMapping("/authenticatedUser")
        public ResponseEntity<UserResponseForTest> test(@Authenticated User user) {
            var response = new UserResponseForTest(
                    user.id(),
                    user.name(),
                    user.role(),
                    user.email(),
                    user.password()
            );
            return ResponseEntity.ok(response);
        }
    }

    private record UserResponseForTest(
            long id,
            String name,
            UserRole role,
            String email,
            String password
    ) {

    }
}
