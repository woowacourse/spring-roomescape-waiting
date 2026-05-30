package roomescape.theme.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.AuthInterceptor;
import roomescape.auth.OwnerOnlyArgumentResolver;
import roomescape.global.config.WebMvcConfig;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.dto.ThemeResult;

@WebMvcTest(ThemeController.class)
@Import({WebMvcConfig.class, AuthInterceptor.class, OwnerOnlyArgumentResolver.class})
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("모든 테마를 성공적으로 조회한다.")
    void readAll_Success() throws Exception {
        // given
        Theme theme = new Theme(1L, "테마", "설명", "url");
        given(themeService.findAll()).willReturn(List.of(ThemeResult.from(theme)));

        // when & then
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("테마"));
    }

    @Test
    @DisplayName("인기 테마를 성공적으로 조회한다.")
    void readPopular_Success() throws Exception {
        // given
        PopularThemesResult result = new PopularThemesResult(List.of(
                new PopularThemeQueryResult(1L, "인기테마", "설명", "url")
        ));
        given(reservationService.findPopularThemes(anyInt(), anyInt())).willReturn(result);

        // when & then
        mockMvc.perform(get("/themes")
                        .param("popular", "true")
                        .param("period", "7")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("인기테마"));
    }

    @Test
    @DisplayName("인기 테마 조회 시 period가 1 미만이면 400 에러를 반환한다.")
    void readPopular_InvalidPeriod_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/themes")
                        .param("popular", "true")
                        .param("period", "0")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 기간 및 개수는 1 이상이어야 합니다."));
    }
}
