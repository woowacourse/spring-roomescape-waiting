package roomescape.theme.presentation;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.member.presentation.resolver.MemberArgumentResolver;
import roomescape.theme.application.ThemeService;
import roomescape.theme.domain.Theme;

@WebMvcTest(value = ThemeController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebMvcConfig.class,
                        AuthorizationInterceptor.class,
                        MemberArgumentResolver.class
                }
        )
)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Nested
    @DisplayName("전체 테마 목록 조회 API")
    class GetThemes {
        @Test
        @DisplayName("테마 목록을 반환한다")
        void returnThemeList() throws Exception {
            // given
            List<ThemeResponse> themeResponses = List.of(
                    new ThemeResponse(new Theme(1L, "테마1", "설명1", "썸네일1")),
                    new ThemeResponse(new Theme(2L, "테마2", "설명2", "썸네일2"))
            );

            doReturn(themeResponses).when(themeService)
                    .getThemes();

            // when & then
            mockMvc.perform(get("/themes"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("테마1"))
                    .andExpect(jsonPath("$[0].description").value("설명1"))
                    .andExpect(jsonPath("$[0].thumbnail").value("썸네일1"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("테마2"))
                    .andExpect(jsonPath("$[1].description").value("설명2"))
                    .andExpect(jsonPath("$[1].thumbnail").value("썸네일2"));
        }

        @Test
        @DisplayName("빈 테마 목록을 반환한다")
        void returnEmptyThemeList() throws Exception {
            // given
            List<ThemeResponse> emptyResponses = List.of();
            doReturn(emptyResponses).when(themeService)
                    .getThemes();

            // when & then
            mockMvc.perform(get("/themes"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("인기 테마 목록 조회 API")
    class GetPopularThemes {
        @Test
        @DisplayName("인기 테마 목록을 반환한다")
        void returnPopularThemeList() throws Exception {
            // given
            List<ThemeResponse> popularThemes = List.of(
                    new ThemeResponse(new Theme(1L, "인기테마1", "인기설명1", "인기썸네일1")),
                    new ThemeResponse(new Theme(2L, "인기테마2", "인기설명2", "인기썸네일2"))
            );

            doReturn(popularThemes).when(themeService)
                    .getPopularThemes();

            // when & then
            mockMvc.perform(get("/themes/popular"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("인기테마1"))
                    .andExpect(jsonPath("$[0].description").value("인기설명1"))
                    .andExpect(jsonPath("$[0].thumbnail").value("인기썸네일1"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("인기테마2"))
                    .andExpect(jsonPath("$[1].description").value("인기설명2"))
                    .andExpect(jsonPath("$[1].thumbnail").value("인기썸네일2"));
        }

        @Test
        @DisplayName("빈 인기 테마 목록을 반환한다")
        void returnEmptyPopularThemeList() throws Exception {
            // given
            List<ThemeResponse> emptyResponses = List.of();
            doReturn(emptyResponses).when(themeService)
                    .getPopularThemes();

            // when & then
            mockMvc.perform(get("/themes/popular"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }
}
