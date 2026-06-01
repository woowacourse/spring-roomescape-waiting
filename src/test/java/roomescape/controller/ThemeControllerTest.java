package roomescape.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.service.ThemeService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("테마 이름이 빈 문자열이면 400을 반환한다.")
    void 테마_이름이_빈_문자열이면_400() throws Exception {
        String body = """
                {"name": "", "description": "설명", "thumbnailUrl": "test.com"}
                """;

        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("테마 설명이 빈 문자열이면 400을 반환한다.")
    void 테마_설명이_빈_문자열이면_400() throws Exception {
        String body = """
                {"name": "테마", "description": "", "thumbnailUrl": "test.com"}
                """;

        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("테마 썸네일 URL이 빈 문자열이면 400을 반환한다.")
    void 테마_썸네일URL이_빈_문자열이면_400() throws Exception {
        String body = """
                {"name": "테마", "description": "설명", "thumbnailUrl": ""}
                """;

        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
