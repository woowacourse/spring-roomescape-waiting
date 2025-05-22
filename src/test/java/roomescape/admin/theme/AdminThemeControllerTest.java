package roomescape.admin.theme;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.member.presentation.resolver.MemberArgumentResolver;
import roomescape.theme.application.ThemeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.presentation.ThemeRequest;
import roomescape.theme.presentation.ThemeResponse;

@WebMvcTest(value = AdminThemeController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebMvcConfig.class,
                        AuthorizationInterceptor.class,
                        MemberArgumentResolver.class
                }
        )
)
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @Nested
    @DisplayName("테마 생성 API")
    class CreateTheme {
        @Test
        @DisplayName("테마를 성공적으로 생성한다")
        void createThemeSuccess() throws Exception {
            // given
            ThemeRequest request = new ThemeRequest(
                    "공포의 방탈출",
                    "무서운 테마의 방탈출입니다",
                    "https://example.com/horror-theme.jpg"
            );

            Theme theme = new Theme(1L, request.getName(), request.getDescription(), request.getThumbnail());
            ThemeResponse response = new ThemeResponse(theme);

            doReturn(response).when(themeService)
                    .createTheme(any(ThemeRequest.class));

            // when & then
            mockMvc.perform(post("/themes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("공포의 방탈출"))
                    .andExpect(jsonPath("$.description").value("무서운 테마의 방탈출입니다"))
                    .andExpect(jsonPath("$.thumbnail").value("https://example.com/horror-theme.jpg"));
        }
    }

    @Nested
    @DisplayName("테마 삭제 API")
    class DeleteTheme {
        @Test
        @DisplayName("테마를 성공적으로 삭제한다")
        void deleteThemeSuccess() throws Exception {
            // given
            Long themeId = 1L;
            doNothing().when(themeService).deleteTheme(themeId);

            // when & then
            mockMvc.perform(delete("/themes/{themeId}", themeId))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("존재하지 않는 테마는 삭제할 수 없다")
        void deleteNonExistentThemeFail() throws Exception {
            // given
            Long nonExistentThemeId = 999L;
            doThrow(new IllegalStateException("이미 삭제되어 있는 리소스입니다."))
                    .when(themeService).deleteTheme(nonExistentThemeId);

            // when & then
            mockMvc.perform(delete("/themes/{themeId}", nonExistentThemeId))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("예약이 존재하는 테마는 삭제할 수 없다")
        void deleteThemeWithReservationsFail() throws Exception {
            // given
            Long themeId = 1L;
            doThrow(new IllegalStateException("예약이 이미 존재하는 테마입니다."))
                    .when(themeService).deleteTheme(themeId);

            // when & then
            mockMvc.perform(delete("/themes/{themeId}", themeId))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}
