package roomescape.domain.theme;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.theme.dto.ThemeRankResponse;
import roomescape.domain.theme.dto.ThemeResponse;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("전체 테마 조회의 요청과 응답을 확인한다.")
    void getAllTheme() throws Exception {
        // given
        ThemeResponse response = new ThemeResponse(
            1L,
            "공포",
            "무서운 테마",
            "theme-url"
        );
        given(themeService.getAllTheme())
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/themes")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("공포"))
            .andExpect(jsonPath("$[0].content").value("무서운 테마"))
            .andExpect(jsonPath("$[0].url").value("theme-url"));

        verify(themeService).getAllTheme();
    }

    @Test
    @DisplayName("테마 랭킹 조회의 요청과 응답을 확인한다.")
    void getThemeRank() throws Exception {
        // given
        ThemeRankResponse response = new ThemeRankResponse(
            1L,
            "공포",
            "theme-url"
        );
        given(themeService.getThemeRank())
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/themes/rank")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].themeName").value("공포"))
            .andExpect(jsonPath("$[0].url").value("theme-url"));

        verify(themeService).getThemeRank();
    }
}
