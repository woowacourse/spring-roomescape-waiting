package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.PopularTheme;
import roomescape.domain.Theme;
import roomescape.dto.theme.response.ThemeReservationTimeResponse;
import roomescape.dto.theme.response.ThemeResponses;
import roomescape.infrastructure.AdminAuthorizationInterceptor;
import roomescape.infrastructure.LoginCheckInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.ThemeService;

@WebMvcTest(controllers = ThemeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, LoginCheckInterceptor.class,
                        AdminAuthorizationInterceptor.class, LoginUserArgumentResolver.class}))
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void GET_themes_목록과_hasNext를_응답한다() throws Exception {
        given(themeService.getThemes(0, 20))
                .willReturn(ThemeResponses.of(
                        List.of(new Theme(1L, "공포", "무서움", "https://thumbnail.url")), false));

        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themes.size()").value(1))
                .andExpect(jsonPath("$.themes[0].name").value("공포"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void GET_themes_id_단건을_응답한다() throws Exception {
        given(themeService.getTheme(1L))
                .willReturn(new Theme(1L, "공포", "무서움", "https://thumbnail.url"));

        mockMvc.perform(get("/themes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("공포"));
    }

    @Test
    void GET_themes_id_서비스가_ResourceNotFoundException을_던지면_404과_메시지를_반환한다() throws Exception {
        given(themeService.getTheme(9999L))
                .willThrow(new roomescape.exception.ResourceNotFoundException("테마", 9999L));

        mockMvc.perform(get("/themes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("테마을(를) 찾을 수 없습니다. id=9999"));
    }

    @Test
    void GET_themes_id_times_예약된_시간은_isReserved_true_나머지는_false() throws Exception {
        given(themeService.getThemeTimes(1L, LocalDate.of(2026, 5, 6)))
                .willReturn(List.of(
                        new ThemeReservationTimeResponse(1L, "10:00", true),
                        new ThemeReservationTimeResponse(2L, "11:00", false)));

        mockMvc.perform(get("/themes/1/times?date=2026-05-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.times.size()").value(2))
                .andExpect(jsonPath("$.times[0].isReserved").value(true))
                .andExpect(jsonPath("$.times[1].isReserved").value(false));
    }

    @Test
    void GET_themes_popular_limit_파라미터를_위임한다() throws Exception {
        given(themeService.getPopularThemes(10))
                .willReturn(List.of(
                        new PopularTheme(new Theme(1L, "1위", "d", "u"), 5L),
                        new PopularTheme(new Theme(2L, "2위", "d", "u"), 3L)));

        mockMvc.perform(get("/themes/popular?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themes.size()").value(2))
                .andExpect(jsonPath("$.themes[0].id").value(1))
                .andExpect(jsonPath("$.themes[0].reservedCount").value(5));
    }

    @Test
    void GET_themes_id_times_date_쿼리_파라미터가_날짜가_아니면_400과_메시지를_반환한다() throws Exception {
        mockMvc.perform(get("/themes/1/times?date=abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("'date' 값 'abc'은(는) yyyy-MM-dd 형식이어야 합니다."));
    }

    @Test
    void GET_themes_popular_limit이_숫자가_아니면_400과_메시지를_반환한다() throws Exception {
        mockMvc.perform(get("/themes/popular?limit=abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("'limit' 값 'abc'은(는) 숫자 형식이어야 합니다."));
    }
}
