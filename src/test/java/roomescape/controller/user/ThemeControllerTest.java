package roomescape.controller.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.domain.PopularTheme;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeAvailability;
import roomescape.service.ReservationAvailabilityService;
import roomescape.service.ThemeService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private ReservationAvailabilityService reservationAvailabilityService;

    @Test
    void 테마_목록을_조회한다() throws Exception {
        // given
        given(themeService.findAll())
                .willReturn(List.of(new Theme(1L, "테마", "설명", "썸네일")));

        // when & then
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("테마"))
                .andExpect(jsonPath("$[0].description").value("설명"))
                .andExpect(jsonPath("$[0].thumbnail").value("썸네일"));

        verify(themeService, times(1)).findAll();
        verifyNoMoreInteractions(themeService, reservationAvailabilityService);
    }

    @Test
    void 예약_가능_시간을_조회한다() throws Exception {
        // given
        given(reservationAvailabilityService.findAvailableTimes(
                eq(1L),
                eq(LocalDate.of(2099, 1, 1))))
                .willReturn(List.of(new TimeAvailability(
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        true)));

        // when & then
        mockMvc.perform(get("/themes/1/times")
                        .param("date", "2099-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].time.id").value(1))
                .andExpect(jsonPath("$[0].time.startAt").value("10:00:00"))
                .andExpect(jsonPath("$[0].available").value(true));

        verify(reservationAvailabilityService, times(1)).findAvailableTimes(1L, LocalDate.of(2099, 1, 1));
        verifyNoMoreInteractions(themeService, reservationAvailabilityService);
    }

    @Test
    void 인기_테마를_조회한다() throws Exception {
        // given
        given(themeService.findWeeklyTopTen(any(LocalDate.class)))
                .willReturn(List.of(new PopularTheme(new Theme(1L, "테마", "설명", "썸네일"), 3L)));

        // when & then
        mockMvc.perform(get("/themes/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("테마"))
                .andExpect(jsonPath("$[0].reservationCount").value(3));

        verify(themeService, times(1)).findWeeklyTopTen(any(LocalDate.class));
        verifyNoMoreInteractions(themeService, reservationAvailabilityService);
    }

    @Test
    void 쿼리_파라미터_형식이_올바르지_않으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/themes/1/times")
                        .param("date", "invalid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("date 형식이 올바르지 않습니다."));

        verifyNoMoreInteractions(themeService, reservationAvailabilityService);
    }

    @Test
    void 경로_변수가_양수가_아니면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/themes/0/times")
                        .param("date", "2099-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id는 양수이어야 합니다."));

        verifyNoMoreInteractions(themeService, reservationAvailabilityService);
    }
}
