package roomescape.domain.theme.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.theme.ThemeService;
import roomescape.domain.theme.admin.dto.AdminThemeResponse;
import roomescape.domain.theme.admin.dto.CreateThemeRequest;
import roomescape.domain.theme.admin.dto.CreateThemeResponse;
import roomescape.support.auth.AdminRequestValidator;
import roomescape.support.exception.ConflictException;
import roomescape.support.exception.errors.ThemeErrors;

@WebMvcTest(AdminThemeController.class)
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private AdminRequestValidator validator;

    @Test
    @DisplayName("관리자가 전체 테마 조회 시 요청과 응답을 확인한다.")
    void getAllThemeForAdmin() throws Exception {
        // given
        AdminThemeResponse response = new AdminThemeResponse(
            1L,
            "공포",
            "무서운 테마",
            "theme-url"
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        given(themeService.getAllThemeForAdmin())
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/admin/themes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("공포"))
            .andExpect(jsonPath("$[0].content").value("무서운 테마"))
            .andExpect(jsonPath("$[0].url").value("theme-url"));

        verify(themeService).getAllThemeForAdmin();
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 전체 테마 조회 시 401을 반환한다.")
    void getAllThemeForAdminWhenUnauthorized() throws Exception {
        // given
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(get("/admin/themes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "wrong-token"))
            .andExpect(status().isUnauthorized());

        verify(themeService, never()).getAllThemeForAdmin();
    }

    @Test
    @DisplayName("관리자가 테마 생성 시 요청과 응답을 확인한다.")
    void createTheme() throws Exception {
        // given
        CreateThemeRequest request = new CreateThemeRequest(
            "공포",
            "무서운 테마",
            "theme-url"
        );
        CreateThemeResponse response = new CreateThemeResponse(
            1L,
            "공포",
            "무서운 테마",
            "theme-url"
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        given(themeService.createTheme(any(CreateThemeRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/admin/themes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("공포"))
            .andExpect(jsonPath("$.content").value("무서운 테마"))
            .andExpect(jsonPath("$.url").value("theme-url"));

        verify(themeService).createTheme(request);
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 테마 생성 시 401을 반환한다.")
    void createThemeWhenUnauthorized() throws Exception {
        // given
        CreateThemeRequest request = new CreateThemeRequest(
            "공포",
            "무서운 테마",
            "theme-url"
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(post("/admin/themes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "wrong-token")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verify(themeService, never()).createTheme(any(CreateThemeRequest.class));
    }

    @Test
    @DisplayName("테마 생성 시 필수 요청값이 누락되면 예외가 발생한다.")
    void createThemeWithoutName() throws Exception {
        // given
        String request = """
            {
              "name": "",
              "content": "무서운 테마",
              "url": "theme-url"
            }
            """;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);

        // when & then
        mockMvc.perform(post("/admin/themes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token")
                .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INPUT_VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("테마 이름은 비어있을 수 없습니다."));
    }

    @Test
    @DisplayName("관리자가 테마 삭제 시 요청과 응답을 확인한다.")
    void deleteTheme() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);

        // when & then
        mockMvc.perform(delete("/admin/themes/{id}", id)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isNoContent());

        verify(themeService).deleteTheme(id);
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 테마 삭제 시 401을 반환한다.")
    void deleteThemeWhenUnauthorized() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/admin/themes/{id}", id)
                .header("X-ADMIN-TOKEN", "wrong-token"))
            .andExpect(status().isUnauthorized());

        verify(themeService, never()).deleteTheme(id);
    }

    @Test
    @DisplayName("이미 예약이 존재하는 테마는 삭제할 수 없다.")
    void deleteThemeWhenThemeInUse() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        willThrow(new ConflictException(ThemeErrors.THEME_IN_USE))
            .given(themeService)
            .deleteTheme(id);

        // when & then
        mockMvc.perform(delete("/admin/themes/{id}", id)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("THEME_IN_USE"))
            .andExpect(jsonPath("$.message").value("이미 예약이 존재하는 테마는 삭제할 수 없습니다."));
    }
}
