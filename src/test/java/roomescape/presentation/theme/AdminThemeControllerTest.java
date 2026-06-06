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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.theme.ThemeService;
import roomescape.common.auth.AdminRequestValidator;
import roomescape.domain.theme.Theme;
import roomescape.presentation.error.GlobalExceptionHandler;
import roomescape.presentation.theme.response.AdminThemesResponse;
import roomescape.presentation.theme.response.CreateThemeResponse;

@DisplayName("관리자 테마 컨트롤러")
@WebMvcTest(controllers = AdminThemeController.class)
@Import(GlobalExceptionHandler.class)
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private AdminRequestValidator validator;

    @Test
    @DisplayName("관리자는 테마 목록을 조회할 수 있다")
    void getAllThemeForAdmin() throws Exception {
        // given
        Theme theme = Theme.of(1L, "심해 공포", "심해 탈출 공포 테마", "/themes/deep-sea");
        given(validator.isUnauthorized(any())).willReturn(false);
        given(themeService.getAllThemeForAdmin()).willReturn(AdminThemesResponse.from(List.of(theme)));

        // when & then
        mockMvc.perform(get("/admin/themes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.themes[0].id").value(1))
                .andExpect(jsonPath("$.themes[0].name").value("심해 공포"))
                .andExpect(jsonPath("$.themes[0].content").value("심해 탈출 공포 테마"))
                .andExpect(jsonPath("$.themes[0].url").value("/themes/deep-sea"));

        verify(validator, times(1)).isUnauthorized(any());
        verify(themeService, times(1)).getAllThemeForAdmin();
    }

    @Test
    @DisplayName("권한이 없으면 테마 목록을 조회할 수 없다")
    void getAllThemeForAdminWhenUnauthorized() throws Exception {
        // given
        given(validator.isUnauthorized(any())).willReturn(true);

        // when & then
        mockMvc.perform(get("/admin/themes"))
                .andExpect(status().isUnauthorized());

        verify(validator, times(1)).isUnauthorized(any());
        verifyNoInteractions(themeService);
    }

    @Test
    @DisplayName("관리자는 테마를 생성할 수 있다")
    void createTheme() throws Exception {
        // given
        Theme theme = Theme.of(1L, "심해 공포", "심해 탈출 공포 테마", "/themes/deep-sea");
        given(validator.isUnauthorized(any())).willReturn(false);
        given(themeService.createTheme(any())).willReturn(CreateThemeResponse.from(theme));

        // when & then
        mockMvc.perform(post("/admin/themes")
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

        verify(validator, times(1)).isUnauthorized(any());
        verify(themeService, times(1)).createTheme(any());
    }

    @Test
    @DisplayName("관리자는 테마를 삭제할 수 있다")
    void deleteTheme() throws Exception {
        // given
        given(validator.isUnauthorized(any())).willReturn(false);

        // when & then
        mockMvc.perform(delete("/admin/themes/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(validator, times(1)).isUnauthorized(any());
        verify(themeService, times(1)).deleteTheme(1L);
    }
}
