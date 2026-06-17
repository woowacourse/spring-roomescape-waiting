package roomescape.theme.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import roomescape.theme.application.service.ThemeService;
import roomescape.theme.presentation.controller.ThemeController;
import roomescape.theme.presentation.dto.ThemeResponse;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void find_all_themes() throws Exception {
        given(themeService.findAll()).willReturn(List.of(
                new ThemeResponse(1L, "theme 1", "description 1", "img 1", 30000L),
                new ThemeResponse(2L, "theme 2", "description 2", "img 2", 30000L)
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
        given(themeService.findPopularThemes(any(), any(), anyInt())).willReturn(List.of(
                new ThemeResponse(1L, "theme 1", "description 1", "img 1", 30000L),
                new ThemeResponse(2L, "theme 2", "description 2", "img 2", 30000L)
        ));

        mockMvc.perform(get("/themes/popular")
                        .queryParam("startAt", "2026-04-29")
                        .queryParam("endAt", "2026-05-05")
                        .queryParam("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("theme 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("theme 2"));
    }
}
