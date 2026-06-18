package roomescape.controller.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ThemeService;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void 전체_테마_조회() throws Exception {
        ThemeResponse response = new ThemeResponse(1L, "우테코 공포물", "레벨2 미션의 공포", "/horror");
        given(themeService.findAllThemes()).willReturn(List.of(response));

        mockMvc.perform(get("/api/themes"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("우테코 공포물"));
    }

    @Test
    void 인기_테마_조회() throws Exception {
        ThemeResponse response = new ThemeResponse(1L, "우테코 공포물", "레벨2 미션의 공포", "/horror");
        given(themeService.findTopTheme(10L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/themes/popular")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("우테코 공포물"));
    }
}
