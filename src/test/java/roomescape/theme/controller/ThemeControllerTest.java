package roomescape.theme.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.theme.service.ThemeService;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ThemeService themeService;

    @MockitoBean
    ReservationService reservationService;

    @DisplayName("period와 limit을 입력받아 인기 테마 목록을 조회하고 200을 반환한다.")
    @Test
    void getPopularThemes_success() throws Exception {
        //given
        when(reservationService.findPopularThemes(anyInt(), anyInt()))
                .thenReturn(new PopularThemesResult(
                        List.of(new PopularThemeQueryResult(1L, "테마", "설명", "url"))
                ));

        //when & then
        mockMvc.perform(
                get("/themes")
                        .param("popular", "true")
                        .param("period", "1")
                        .param("limit", "1")
        ).andExpect(status().isOk());
    }

    @DisplayName("인기 테마 목록을 조회 시, period가 없으면 400을 반환한다.")
    @Test
    void getPopularThemes_no_period() throws Exception {
        mockMvc.perform(
                get("/themes")
                        .param("popular", "true")
                        .param("limit", "1")
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("인기 테마 목록을 조회 시, period 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void getPopularThemes_invalid_period() throws Exception {
        mockMvc.perform(
                get("/themes")
                        .param("popular", "true")
                        .param("period", "invalid")
                        .param("limit", "1")
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("인기 테마 목록을 조회 시, limit이 없으면 400을 반환한다.")
    @Test
    void getPopularThemes_no_limit() throws Exception {
        mockMvc.perform(
                get("/themes")
                        .param("popular", "true")
                        .param("period", "1")
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("인기 테마 목록을 조회 시, limit 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void getPopularThemes_invalid_limit() throws Exception {
        mockMvc.perform(
                get("/themes")
                        .param("popular", "true")
                        .param("period", "1")
                        .param("limit", "invalid")
        ).andExpect(status().isBadRequest());
    }
}
