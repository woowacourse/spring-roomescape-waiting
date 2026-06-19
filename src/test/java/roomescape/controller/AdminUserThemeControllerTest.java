package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.global.auth.AdminAuthorizationInterceptor;
import roomescape.global.exception.DomainErrorHttpMapper;
import roomescape.global.config.WebConfig;
import roomescape.service.AuthService;
import roomescape.service.ThemeService;

@WebMvcTest(AdminThemeController.class)
@Import({DomainErrorHttpMapper.class, AdminAuthorizationInterceptor.class, WebConfig.class})
class AdminUserThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private AuthService authService;

    @DisplayName("관리자는 테마를 생성한다.")
    @Test
    void create() throws Exception {
        given(authService.getLoginMember(7L)).willReturn(admin());
        given(themeService.saveTheme(any())).willReturn(1L);

        mockMvc.perform(post("/admin/themes")
                        .session(adminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "잠긴 방",
                                  "description": "설명",
                                  "thumbnailUrl": "https://example.com/theme.jpg",
                                  "price": 20000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/themes/1"));
    }

    @DisplayName("테마 생성 요청 값이 올바르지 않으면 400을 반환한다.")
    @Test
    void createInvalidRequest() throws Exception {
        given(authService.getLoginMember(7L)).willReturn(admin());

        mockMvc.perform(post("/admin/themes")
                        .session(adminSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "설명",
                                  "thumbnailUrl": "https://example.com/theme.jpg"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @DisplayName("관리자는 테마를 삭제한다.")
    @Test
    void deleteTheme() throws Exception {
        given(authService.getLoginMember(7L)).willReturn(admin());

        mockMvc.perform(delete("/admin/themes/1").session(adminSession()))
                .andExpect(status().isNoContent());

        verify(themeService).deleteTheme(1L);
    }

    @DisplayName("참조 중인 테마 삭제는 422를 반환한다.")
    @Test
    void deleteReferencedTheme() throws Exception {
        given(authService.getLoginMember(7L)).willReturn(admin());
        org.mockito.Mockito.doThrow(new RoomescapeException(
                        DomainErrorCode.REFERENTIAL_INTEGRITY,
                        "이 테마를 참조하는 예약이 있어 삭제할 수 없습니다."
                ))
                .when(themeService)
                .deleteTheme(1L);

        mockMvc.perform(delete("/admin/themes/1").session(adminSession()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("REFERENTIAL_INTEGRITY"));
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.LOGIN_MEMBER_ID, 7L);
        return session;
    }

    private Member admin() {
        return new Member(7L, "admin", "관리자", "password", Role.ADMIN);
    }
}
