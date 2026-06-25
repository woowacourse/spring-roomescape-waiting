package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.ThemePatchRequest;
import roomescape.controller.dto.ThemeRequest;
import roomescape.domain.Theme;
import roomescape.exception.ProblemDetailsAdvice;
import roomescape.service.ThemeService;

@WebMvcTest(ThemeController.class)
@Import(ProblemDetailsAdvice.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("전체 테마 목록을 조회하고 200 상태 코드를 반환한다.")
    void getThemes() throws Exception {
        given(themeService.allTheme()).willReturn(List.of(createMockTheme()));
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("식별자로 단건 테마를 조회하고 200 상태 코드를 반환한다.")
    void getThemeById() throws Exception {
        given(themeService.findThemeById(anyLong())).willReturn(createMockTheme());
        mockMvc.perform(get("/themes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("공포"));
    }

    @Test
    @DisplayName("인기 테마 목록을 조회하고 200 상태 코드를 반환한다.")
    void getPopularThemes() throws Exception {
        given(themeService.findPopularThemes(anyLong(), anyLong())).willReturn(List.of(createMockTheme()));
        mockMvc.perform(get("/themes").param("topCount", "5").param("during", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("유효한 데이터로 테마를 생성하고 201 상태 코드와 Location 헤더를 반환한다.")
    void createTheme() throws Exception {
        given(themeService.saveTheme(any())).willReturn(createMockTheme());
        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ThemeRequest("공포", "귀신", "https://url"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("테마를 삭제하고 204 상태 코드를 반환한다.")
    void deleteTheme() throws Exception {
        doNothing().when(themeService).removeTheme(anyLong());
        mockMvc.perform(delete("/themes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("테마의 전체 정보를 수정(PUT)하고 200 상태 코드를 반환한다.")
    void updateTheme() throws Exception {
        given(themeService.putTheme(anyLong(), any())).willReturn(createMockTheme());
        mockMvc.perform(put("/themes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ThemeRequest("공포", "귀신", "https://url"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("공포"));
    }

    @Test
    @DisplayName("테마의 일부 정보를 수정(PATCH)하고 200 상태 코드를 반환한다.")
    void patchTheme() throws Exception {
        given(themeService.patchTheme(anyLong(), any())).willReturn(createMockTheme());
        mockMvc.perform(patch("/themes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ThemePatchRequest("공포", null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("공포"));
    }

    private Theme createMockTheme() {
        return new Theme(1L, "공포", "귀신의 집", "https://url");
    }
}
