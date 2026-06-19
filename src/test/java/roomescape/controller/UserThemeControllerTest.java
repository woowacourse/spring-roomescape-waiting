package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.dto.PopularThemeResult;
import roomescape.service.dto.ReservationTimeResult;
import roomescape.service.dto.ThemeResult;

@WebMvcTest(UserThemeController.class)
class UserThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ThemeService themeService;
    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("GET /user/themes - 테마 목록을 반환한다")
    void list() throws Exception {
        given(themeService.findAll()).willReturn(List.of(sampleTheme()));

        mockMvc.perform(get("/user/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("무인도 탈출"));
    }

    @Test
    @DisplayName("GET /user/themes/{themeId}/available-times - 가용 시간을 반환한다")
    void availableTimes() throws Exception {
        given(reservationTimeService.findAvailable(any(), eq(1L)))
                .willReturn(List.of(new ReservationTimeResult(1L, LocalTime.of(10, 0))));

        mockMvc.perform(get("/user/themes/1/available-times").param("date", "2099-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00"));
    }

    @Test
    @DisplayName("GET /user/themes/popular - 인기 테마 목록을 반환한다")
    void popular() throws Exception {
        given(themeService.findPopular())
                .willReturn(List.of(new PopularThemeResult(sampleTheme(), 5)));

        mockMvc.perform(get("/user/themes/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].reservationCount").value(5));
    }

    private ThemeResult sampleTheme() {
        return new ThemeResult(1L, "무인도 탈출", "설명", "https://example.com/thumb.jpg", 0L);
    }
}
