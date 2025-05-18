package roomescape.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.TestFixture;
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
        // given
        ThemeResult themeResult = TestFixture.createThemeResult(1L, 
                TestFixture.TEST_THEME_NAME, 
                TestFixture.TEST_THEME_DESCRIPTION, 
                TestFixture.TEST_THEME_THUMBNAIL);
                
        when(themeService.create(any(CreateThemeParam.class))).thenReturn(themeResult);

        // when & then
        mockMvc.perform(post("/themes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestFixture.createThemeJson()))
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
        // given
        ThemeResult themeResult = TestFixture.createThemeResult(1L, 
                TestFixture.TEST_THEME_NAME, 
                TestFixture.TEST_THEME_DESCRIPTION, 
                TestFixture.TEST_THEME_THUMBNAIL);
                
        when(themeService.findAll()).thenReturn(List.of(themeResult));

        // when & then
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