package roomescape.theme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import roomescape.theme.controller.dto.ThemeListResponse;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
                Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png"),
                Theme.of(2L, "레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme-2.png"),
                Theme.of(3L, "레벨4 탈출", "우테코 레벨4를 탈출하는 내용입니다.", "https://example.com/theme-3.png")
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
    void popularThemes() throws Exception {
        // given
        List<Theme> themes = List.of(
                Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png"),
                Theme.of(2L, "레벨3 탈출", "우테코 레벨3을 탈출하는 내용입니다.", "https://example.com/theme-2.png"),
                Theme.of(3L, "레벨4 탈출", "우테코 레벨4를 탈출하는 내용입니다.", "https://example.com/theme-3.png")
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

    @Test
    @DisplayName("인기 테마 목록을 조회할 때 요청 파라미터가 없으면 기본값으로 조회한다.")
    void popularThemes_default() throws Exception {
        // given
        given(themeService.findPopularThemes(anyInt(), anyInt()))
                .willReturn(List.of());

        // when then
        mockMvc.perform(get("/themes/popularity"))
                .andDo(print())
                .andExpect(status().isOk());

        then(themeService).should()
                .findPopularThemes(eq(7), eq(10));
    }

    @Test
    @DisplayName("인기 테마 목록을 조회할 때 요청 파라미터가 비어있으면 기본값으로 조회한다.")
    void popularThemes_default_when_blank() throws Exception {
        // given
        given(themeService.findPopularThemes(anyInt(), anyInt()))
                .willReturn(List.of());

        // when then
        mockMvc.perform(get("/themes/popularity")
                        .param("days", "")
                        .param("size", ""))
                .andDo(print())
                .andExpect(status().isOk());

        then(themeService).should()
                .findPopularThemes(eq(7), eq(10));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 10, 최소 하루 단위로 인기테마를 조회할 수 있습니다.",
            "7, 0, 최소 1개 이상 조회해야 합니다.",
            "7, 11, 최대 10개까지 인기테마를 조회할 수 있습니다."
    })
    @DisplayName("인기 테마 목록을 조회할 때 요청 파라미터 검증에 실패하면 에러가 발생한다.")
    void popularThemes_fail_when_invalid_request(String days, String size, String message) throws Exception {
        // when then
        mockMvc.perform(get("/themes/popularity")
                        .param("days", days)
                        .param("size", size))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.messages[0]").value(message));

        then(themeService).should(never())
                .findPopularThemes(anyInt(), anyInt());
    }

}
