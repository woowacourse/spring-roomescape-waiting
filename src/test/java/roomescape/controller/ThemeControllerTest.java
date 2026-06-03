package roomescape.controller;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
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
import roomescape.infrastructure.AuthInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.ThemeService;

@WebMvcTest(controllers = ThemeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, AuthInterceptor.class, LoginUserArgumentResolver.class}))
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("GET /themes - 목록과 hasNext를 응답한다")
    void getThemesRespondsWithListAndHasNext() throws Exception {
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
    @DisplayName("GET /themes/{id} - 단건을 응답한다")
    void getThemeRespondsWithSingle() throws Exception {
        given(themeService.getTheme(1L))
                .willReturn(new Theme(1L, "공포", "무서움", "https://thumbnail.url"));

        mockMvc.perform(get("/themes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("공포"));
    }

    @Test
    @DisplayName("GET /themes/{id} - 서비스가 ResourceNotFoundException을 던지면 404과 메시지를 반환한다")
    void getThemeReturns404OnResourceNotFoundException() throws Exception {
        given(themeService.getTheme(9999L))
                .willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "테마", 9999L));

        mockMvc.perform(get("/themes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /themes/{id}/times - 예약된 시간은 isReserved true, 나머지는 false")
    void getThemeTimesMarksReservedTimes() throws Exception {
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
    @DisplayName("GET /themes/popular - limit 파라미터를 위임한다")
    void getPopularThemesDelegatesLimitParameter() throws Exception {
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
    @DisplayName("GET /themes/{id}/times - date 쿼리 파라미터가 날짜가 아니면 400과 메시지를 반환한다")
    void getThemeTimesReturns400WhenDateIsInvalid() throws Exception {
        mockMvc.perform(get("/themes/1/times?date=abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("GET /themes/popular - limit이 숫자가 아니면 400과 메시지를 반환한다")
    void getPopularThemesReturns400WhenLimitIsNotNumber() throws Exception {
        mockMvc.perform(get("/themes/popular?limit=abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
