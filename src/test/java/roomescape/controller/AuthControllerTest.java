package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.global.exception.DomainErrorHttpMapper;
import roomescape.service.AuthService;

@WebMvcTest(AuthController.class)
@Import(DomainErrorHttpMapper.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @DisplayName("로그인 성공 시 회원 정보를 반환하고 세션을 생성한다.")
    @Test
    void login() throws Exception {
        given(authService.login("roro", "password"))
                .willReturn(new Member(1L, "roro", "러로", "password", Role.USER));

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "roro",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.loginId").value("roro"))
                .andExpect(jsonPath("$.name").value("러로"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        assertThat(result.getRequest().getSession())
                .isNotNull()
                .extracting(session -> session.getAttribute(AuthService.LOGIN_MEMBER_ID))
                .isEqualTo(1L);
    }

    @DisplayName("로그인 실패 시 401을 반환한다.")
    @Test
    void invalidLogin() throws Exception {
        given(authService.login("roro", "wrong"))
                .willThrow(new RoomescapeException(
                        DomainErrorCode.INVALID_LOGIN,
                        "로그인 ID 또는 비밀번호가 올바르지 않습니다."
                ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginId": "roro",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_LOGIN"));
    }

    @DisplayName("회원가입 성공 시 회원 정보를 반환하고 세션을 생성한다.")
    @Test
    void signup() throws Exception {
        given(authService.signup(new roomescape.controller.dto.SignupRequest("새회원", "new-user", "password", "password")))
                .willReturn(new Member(2L, "new-user", "새회원", "password", Role.USER));

        MvcResult result = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새회원",
                                  "loginId": "new-user",
                                  "password": "password",
                                  "passwordConfirm": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.loginId").value("new-user"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        assertThat(result.getRequest().getSession())
                .isNotNull()
                .extracting(session -> session.getAttribute(AuthService.LOGIN_MEMBER_ID))
                .isEqualTo(2L);
    }

    @DisplayName("중복 로그인 ID로 회원가입하면 409를 반환한다.")
    @Test
    void signupDuplicateLoginId() throws Exception {
        given(authService.signup(new roomescape.controller.dto.SignupRequest("새회원", "roro", "password", "password")))
                .willThrow(new RoomescapeException(DomainErrorCode.DUPLICATE_MEMBER, "이미 사용 중인 로그인 ID입니다."));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새회원",
                                  "loginId": "roro",
                                  "password": "password",
                                  "passwordConfirm": "password"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_MEMBER"));
    }

    @DisplayName("로그인 회원 정보를 조회한다.")
    @Test
    void me() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, 1L);
        given(authService.getLoginMember(1L))
                .willReturn(new Member(1L, "roro", "러로", "password", Role.USER));

        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("러로"));
    }

    @DisplayName("세션이 없으면 로그인 회원 조회 시 401을 반환한다.")
    @Test
    void meUnauthenticated() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @DisplayName("로그아웃은 세션을 만료한다.")
    @Test
    void logout() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(delete("/auth/logout").session(session))
                .andExpect(status().isNoContent());
    }
}
