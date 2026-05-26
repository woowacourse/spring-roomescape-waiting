package roomescape.theme.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.theme.application.dto.PopularThemeQueryResult;
import roomescape.theme.application.dto.ThemeQueryResult;
import roomescape.theme.application.service.ThemeService;
import roomescape.theme.presentation.controller.ThemeController;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void find_all_themes() throws Exception {
        given(themeService.findAll()).willReturn(List.of(
                ThemeQueryResult.from(1L, "theme 1", "description 1", "img 1"),
                ThemeQueryResult.from(2L, "theme 2", "description 2", "img 2")
        ));

        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("theme 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("theme 2"));
    }

    @Test
    void find_popular_themes() throws Exception {
        given(themeService.findPopularThemes(org.mockito.ArgumentMatchers.any())).willReturn(List.of(
                new PopularThemeQueryResult(1L, "theme 1", "description 1", "img 1", 2),
                new PopularThemeQueryResult(2L, "theme 2", "description 2", "img 2", 1)
        ));

        mockMvc.perform(get("/themes/popular-top-10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reservedCount").value(2))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].reservedCount").value(1));
    }
}
