package roomescape.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.service.MemberService;
import roomescape.service.ThemeService;
import roomescape.service.param.CreateThemeParam;
import roomescape.service.result.ThemeResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    private static final String TEST_THEME_NAME = "테마1";
    private static final String TEST_THEME_DESCRIPTION = "description";
    private static final String TEST_THEME_THUMBNAIL = "thumbnail";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CookieProvider cookieProvider;

    @Test
    @DisplayName("테마를 생성할 수 있다.")
    void createTheme() throws Exception {
        String themeJson = String.format("""
                {
                    "name": "%s",
                    "description": "%s",
                    "thumbnail": "%s"
                }
                """, TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);

        ThemeResult themeResult = new ThemeResult(1L, TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);
        when(themeService.create(any(CreateThemeParam.class))).thenReturn(themeResult);

        mockMvc.perform(post("/themes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(themeJson)
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(themeResult.id()))
                .andExpect(jsonPath("$.name").value(themeResult.name()))
                .andExpect(jsonPath("$.description").value(themeResult.description()))
                .andExpect(jsonPath("$.thumbnail").value(themeResult.thumbnail()));
    }

    @Test
    @DisplayName("테마 목록을 조회할 수 있다.")
    void getThemes() throws Exception {
        ThemeResult themeResult = new ThemeResult(1L, TEST_THEME_NAME, TEST_THEME_DESCRIPTION, TEST_THEME_THUMBNAIL);
        when(themeService.findAll()).thenReturn(List.of(themeResult));

        mockMvc.perform(get("/themes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(themeResult.id()))
                .andExpect(jsonPath("$[0].name").value(themeResult.name()))
                .andExpect(jsonPath("$[0].description").value(themeResult.description()))
                .andExpect(jsonPath("$[0].thumbnail").value(themeResult.thumbnail()));
    }

    @Test
    @DisplayName("테마를 삭제할 수 있다.")
    void deleteTheme() throws Exception {
        mockMvc.perform(delete("/themes/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
} 