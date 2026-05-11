package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import roomescape.controller.dto.ThemeListResponse;
import roomescape.controller.dto.ThemeResponse;
import roomescape.domain.Theme;
import roomescape.service.ThemeService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;


    @Test
    @DisplayName("테마 목록을 조회한다.")
    void getThemeList() throws Exception {
        // given
        List<Theme> themes = List.of(
                new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png"),
                new Theme(2L, "레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme-2.png"),
                new Theme(3L, "레벨4 탈출", "우테코 레벨4를 탈출하는 내용입니다.", "https://example.com/theme-3.png")
        );
        given(themeService.findAllThemes()).willReturn(themes);

        // when then
        MvcResult result = mockMvc.perform(get("/themes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ThemeListResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ThemeListResponse.class
        );

        assertThat(response.themes()).hasSize(3);
        assertThat(response.themes())
                .extracting(
                        ThemeResponse::id,
                        ThemeResponse::name,
                        ThemeResponse::description,
                        ThemeResponse::thumbnail
                )
                .containsExactly(
                        tuple(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png"),
                        tuple(2L, "레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme-2.png"),
                        tuple(3L, "레벨4 탈출", "우테코 레벨4를 탈출하는 내용입니다.", "https://example.com/theme-3.png")
                );

        then(themeService)
                .should()
                .findAllThemes();
    }

    @Test
    @DisplayName("인기 테마 목록을 조회한다.")
    public void popularThemes() throws Exception {
        // given
        List<Theme> themes = List.of(
                new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png"),
                new Theme(2L, "레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme-2.png"),
                new Theme(3L, "레벨4 탈출", "우테코 레벨4를 탈출하는 내용입니다.", "https://example.com/theme-3.png")
        );
        given(themeService.findPopularThemes(anyInt(), anyInt()))
                .willReturn(themes);

        // when then
        MvcResult result = mockMvc.perform(get("/themes/popularity")
                        .param("days", "7")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ThemeListResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ThemeListResponse.class
        );

        assertThat(response.themes())
                .extracting(
                        ThemeResponse::id,
                        ThemeResponse::name,
                        ThemeResponse::description,
                        ThemeResponse::thumbnail
                )
                .containsExactly(
                        tuple(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png"),
                        tuple(2L, "레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme-2.png"),
                        tuple(3L, "레벨4 탈출", "우테코 레벨4를 탈출하는 내용입니다.", "https://example.com/theme-3.png")
                );

        then(themeService).should()
                .findPopularThemes(eq(7), eq(10));

    }

}
