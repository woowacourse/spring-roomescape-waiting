package roomescape.presentation.theme;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import roomescape.application.theme.response.PopularThemesResponse;
import roomescape.application.theme.response.ThemesResponse;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRankResult;
import roomescape.presentation.error.GlobalExceptionHandler;

@DisplayName("테마 컨트롤러")
@WebMvcTest(controllers = ThemeController.class)
@Import(GlobalExceptionHandler.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("테마 목록을 조회할 수 있다")
    void getAllTheme() throws Exception {
        // given
        Theme theme = Theme.of(1L, "심해 공포", "심해 탈출 공포 테마", "/themes/deep-sea");
        given(themeService.getAllTheme()).willReturn(ThemesResponse.from(List.of(theme)));

        // when & then
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.themes[0].id").value(1))
                .andExpect(jsonPath("$.themes[0].name").value("심해 공포"))
                .andExpect(jsonPath("$.themes[0].content").value("심해 탈출 공포 테마"))
                .andExpect(jsonPath("$.themes[0].url").value("/themes/deep-sea"));

        verify(themeService, times(1)).getAllTheme();
    }

    @Test
    @DisplayName("인기 테마를 조회할 수 있다")
    void getThemeRank() throws Exception {
        // given
        Theme theme = Theme.of(1L, "도심 추격전", "도심에서 벌어지는 추격 테마", "/themes/chase");
        given(themeService.getThemeRank()).willReturn(
                PopularThemesResponse.from(List.of(ThemeRankResult.of(theme, 1))));

        // when & then
        mockMvc.perform(get("/themes/rank"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.popularThemes[0].id").value(1))
                .andExpect(jsonPath("$.popularThemes[0].name").value("도심 추격전"))
                .andExpect(jsonPath("$.popularThemes[0].thumbnailUrl").value("/themes/chase"))
                .andExpect(jsonPath("$.popularThemes[0].rank").value(1));

        verify(themeService, times(1)).getThemeRank();
    }
}
