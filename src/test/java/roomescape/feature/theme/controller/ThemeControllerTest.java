package roomescape.feature.theme.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.feature.theme.dto.response.ThemeResponseDto;
import roomescape.feature.theme.service.ThemeService;
import roomescape.fixture.ThemeFixture;
import roomescape.support.WebMvcControllerTest;

@WebMvcControllerTest(controllers = ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Nested
    class 테마_목록_조회 {

        @Test
        void 활성_테마_목록을_조회한다() throws Exception {
            when(themeService.getThemes()).thenReturn(List.of(
                new ThemeResponseDto(1L, ThemeFixture.VALID.getName(), ThemeFixture.VALID.getDescription(),
                    ThemeFixture.VALID.getImageUrl(), false),
                new ThemeResponseDto(2L, ThemeFixture.VALID_ANOTHER.getName(),
                    ThemeFixture.VALID_ANOTHER.getDescription(), ThemeFixture.VALID_ANOTHER.getImageUrl(), false)
            ));

            mockMvc.perform(get("/api/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        void 테마가_없으면_빈_목록을_반환한다() throws Exception {
            when(themeService.getThemes()).thenReturn(List.of());

            mockMvc.perform(get("/api/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    class 인기_테마_조회 {

        @Test
        void 인기_테마_목록을_조회한다() throws Exception {
            when(themeService.getPopularThemes()).thenReturn(List.of(
                new ThemeResponseDto(1L, ThemeFixture.VALID.getName(), ThemeFixture.VALID.getDescription(),
                    ThemeFixture.VALID.getImageUrl(), false)
            ));

            mockMvc.perform(get("/api/themes/rankings/last-7-days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        void 인기_테마가_없으면_빈_목록을_반환한다() throws Exception {
            when(themeService.getPopularThemes()).thenReturn(List.of());

            mockMvc.perform(get("/api/themes/rankings/last-7-days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}
