package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.ThemeRequest;
import roomescape.service.ThemeService;
import roomescape.service.dto.ThemeResult;

@WebMvcTest(AdminThemeController.class)
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("GET /admin/themes - 테마 목록을 반환한다")
    void list() throws Exception {
        given(themeService.findAll()).willReturn(List.of(sampleResult()));

        mockMvc.perform(get("/admin/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("무인도 탈출"));
    }

    @Test
    @DisplayName("POST /admin/themes - 유효한 요청이면 테마를 생성한다")
    void create() throws Exception {
        given(themeService.create(any())).willReturn(sampleResult());
        ThemeRequest request = new ThemeRequest("무인도 탈출", "설명", "https://example.com/thumb.jpg");

        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("무인도 탈출"));
    }

    @Test
    @DisplayName("POST /admin/themes - 이름이 비어있으면 400을 반환한다")
    void create_invalid() throws Exception {
        ThemeRequest request = new ThemeRequest("", "설명", "https://example.com/thumb.jpg");

        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/themes/{id} - 테마를 삭제한다")
    void delete_() throws Exception {
        mockMvc.perform(delete("/admin/themes/1"))
                .andExpect(status().isOk());
    }

    private ThemeResult sampleResult() {
        return new ThemeResult(1L, "무인도 탈출", "설명", "https://example.com/thumb.jpg");
    }
}
