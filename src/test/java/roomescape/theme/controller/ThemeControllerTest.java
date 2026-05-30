package roomescape.theme.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeSaveServiceRequest;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @DisplayName("전체 테마를 조회한다.")
    @Test
    void 테마_목록_조회() throws Exception {
        Theme theme1 = new Theme("이름1", "설명1", "https://img.test/1.png").withId(1L);
        Theme theme2 = new Theme("이름2", "설명2", "https://img.test/2.png").withId(2L);

        when(themeService.getAll()).thenReturn(List.of(theme1, theme2));

        mockMvc.perform(get("/themes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("이름1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("이름2"));
    }

    @DisplayName("이름, 설명으로 테마를 생성 후 201을 반환한다.")
    @Test
    void 테마_생성() throws Exception {
        Theme saved = new Theme("라이", "설명", "https://img.test/a.png").withId(1L);
        ThemeSaveServiceRequest request = new ThemeSaveServiceRequest("라이", "설명", "https://img.test/a.png");
        when(themeService.create(request)).thenReturn(saved);

        String requestBody = """
                {
                    "name": "라이",
                    "description": "설명",
                    "imageUrl": "https://img.test/a.png"
                }
                """;

        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("라이"))
                .andExpect(jsonPath("$.description").value("설명"))
                .andExpect(jsonPath("$.imageUrl").value("https://img.test/a.png"));
    }

    @DisplayName("테마 ID로 테마를 삭제 후 204를 반환한다.")
    @Test
    void 테마_삭제() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/themes/{id}", id))
                .andExpect(status().isNoContent());

        verify(themeService).deleteById(id);
    }

    @DisplayName("존재하지 않는 테마 ID로 삭제 요청인 경우, 404를 반환한다.")
    @Test
    void 존재하지_않는_테마_삭제_404() throws Exception {
        Long id = 999L;
        doThrow(new ThemeNotFoundException(id))
                .when(themeService).deleteById(id);

        mockMvc.perform(delete("/themes/{id}", id))
                .andExpect(status().isNotFound());
    }

    @DisplayName("이름 없이 테마 생성 요청인 경우, 400을 반환한다.")
    @Test
    void 빈_이름_테마_생성_400() throws Exception {
        String requestBody = """
                {
                    "name": "",
                    "description": "설명",
                    "imageUrl": "https://img.test/a.png"
                }
                """;

        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("공백 이름으로 테마 생성 요청인 경우, 400을 반환한다.")
    @Test
    void 공백_이름_테마_생성_400() throws Exception {
        String requestBody = """
                {
                    "name": "   ",
                    "description": "설명",
                    "imageUrl": "https://img.test/a.png"
                }
                """;

        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("설명 없이 테마 생성 요청인 경우, 400을 반환한다.")
    @Test
    void 빈_설명_테마_생성_400() throws Exception {
        String requestBody = """
                {
                    "name": "이름",
                    "description": "",
                    "imageUrl": "https://img.test/a.png"
                }
                """;

        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("인기 테마 목록을 조회한다.")
    @Test
    void 인기_테마_목록_조회() throws Exception {
        Theme top1 = new Theme("1위", "설명1", "https://img.test/1.png").withId(1L);
        Theme top2 = new Theme("2위", "설명2", "https://img.test/2.png").withId(2L);

        when(themeService.getBestThemes()).thenReturn(List.of(top1, top2));

        mockMvc.perform(get("/themes/best")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("1위"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("2위"));
    }

    @DisplayName("인기 테마가 없으면 빈 목록을 반환한다.")
    @Test
    void 인기_테마_빈_목록_조회() throws Exception {
        when(themeService.getBestThemes()).thenReturn(List.of());

        mockMvc.perform(get("/themes/best")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
