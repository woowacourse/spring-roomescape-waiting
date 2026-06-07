package roomescape.presentation.theme;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.theme.ThemeService;
import roomescape.application.theme.response.AdminThemesResponse;
import roomescape.application.theme.response.CreateThemeResponse;
import roomescape.common.auth.AdminAccessInterceptor;
import roomescape.common.auth.LoginUserArgumentResolver;
import roomescape.common.auth.SessionKeys;
import roomescape.common.config.AuthWebConfig;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRole;
import roomescape.presentation.error.GlobalExceptionHandler;

@DisplayName("관리자 테마 컨트롤러")
@WebMvcTest(controllers = AdminThemeController.class)
@Import({
        GlobalExceptionHandler.class,
        AuthWebConfig.class,
        LoginUserArgumentResolver.class,
        AdminAccessInterceptor.class
})
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("관리자는 테마 목록을 조회할 수 있다")
    void getAllThemeForAdmin() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.LOGIN_USER, User.of(1L, "admin", "", UserRole.ADMIN));
        Theme theme = Theme.of(1L, "심해 공포", "심해 탈출 공포 테마", "/themes/deep-sea");
        given(themeService.getAllThemeForAdmin()).willReturn(AdminThemesResponse.from(List.of(theme)));

        // when & then
        mockMvc.perform(get("/admin/themes").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.themes[0].id").value(1))
                .andExpect(jsonPath("$.themes[0].name").value("심해 공포"))
                .andExpect(jsonPath("$.themes[0].content").value("심해 탈출 공포 테마"))
                .andExpect(jsonPath("$.themes[0].url").value("/themes/deep-sea"));

        verify(themeService, times(1)).getAllThemeForAdmin();
    }

    @Test
    @DisplayName("권한이 없으면 테마 목록을 조회할 수 없다")
    void getAllThemeForAdminWhenUnauthorized() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.LOGIN_USER, User.of(10L, "홍길동", "", UserRole.USER));

        // when & then
        mockMvc.perform(get("/admin/themes").session(session))
                .andExpect(status().isForbidden());

        verifyNoInteractions(themeService);
    }

    @Test
    @DisplayName("관리자는 테마를 생성할 수 있다")
    void createTheme() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.LOGIN_USER, User.of(1L, "admin", "", UserRole.ADMIN));
        Theme theme = Theme.of(1L, "심해 공포", "심해 탈출 공포 테마", "/themes/deep-sea");
        given(themeService.createTheme(any())).willReturn(CreateThemeResponse.from(theme));

        // when & then
        mockMvc.perform(post("/admin/themes")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "심해 공포",
                                  "content": "심해 탈출 공포 테마",
                                  "thumbnailUrl": "/themes/deep-sea"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("심해 공포"))
                .andExpect(jsonPath("$.content").value("심해 탈출 공포 테마"))
                .andExpect(jsonPath("$.url").value("/themes/deep-sea"));

        verify(themeService, times(1)).createTheme(any());
    }

    @Test
    @DisplayName("관리자는 테마를 삭제할 수 있다")
    void deleteTheme() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.LOGIN_USER, User.of(1L, "admin", "", UserRole.ADMIN));

        // when & then
        mockMvc.perform(delete("/admin/themes/{id}", 1L).session(session))
                .andExpect(status().isNoContent());

        verify(themeService, times(1)).deleteTheme(1L);
    }
}
