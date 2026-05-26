package roomescape.ui.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.holiday.domain.Holiday;
import roomescape.holiday.exception.HolidayNotFoundException;
import roomescape.holiday.service.HolidayService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.time.domain.ReservationTime;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationSaveServiceDto;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.service.ThemeService;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.service.TimeService;

@WebMvcTest(RoomescapePageController.class)
class RoomescapePageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private TimeService timeService;

    @MockitoBean
    private HolidayService holidayService;

    private void stubDashboardData() {
        ReservationTime time = new ReservationTime(1L, LocalDateTime.of(2026, 5, 6, 10, 0), LocalDateTime.of(2026, 5, 6, 11, 0));
        Theme theme = new Theme("미궁의 유산", "고대 미궁", "https://example.com/theme.png").withId(1L);
        Mockito.when(reservationService.getAll()).thenReturn(List.of(
                new Reservation("브라운", time, theme, Status.RESERVED, LocalDateTime.now())
                        .withId(1L)));
        Mockito.when(themeService.getAll()).thenReturn(List.of(
                new Theme("미궁의 유산", "고대 미궁", "https://example.com/theme.png").withId(1L)
        ));
        Mockito.when(themeService.getBestThemes()).thenReturn(List.of(
                new Theme("미궁의 유산", "고대 미궁", "https://example.com/theme.png").withId(1L)
        ));
        Mockito.when(timeService.findAll()).thenReturn(List.of(time));
        Mockito.when(holidayService.getAll()).thenReturn(List.of(
                new Holiday(1L, LocalDate.of(2026, 5, 7))
        ));
        Mockito.when(themeService.getAvailableTimes(1L, LocalDate.of(2026, 5, 6))).thenReturn(List.of(time));
    }

    @Test
    void dashboardPageRenders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("방탈출 운영 화면")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("예약 관리")));
    }

    @Test
    void reservationsPageRendersReservationData() throws Exception {
        stubDashboardData();

        mockMvc.perform(get("/dashboard/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("브라운")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("미궁의 유산")));
    }

    @Test
    void availabilityPageRendersThemeOptionsAndResults() throws Exception {
        stubDashboardData();

        mockMvc.perform(get("/dashboard/availability")
                        .param("availableThemeId", "1")
                        .param("availableDate", "2026-05-06"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("테마 선택")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("미궁의 유산")));
    }

    @Test
    void createReservation_redirectsWithSuccessMessage() throws Exception {
        mockMvc.perform(post("/dashboard/reservations")
                        .param("name", "브라운")
                        .param("themeId", "1")
                        .param("timeId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/reservations"))
                .andExpect(flash().attribute("successMessage", "예약을 생성했습니다."));

        Mockito.verify(reservationService).create(Mockito.any(ReservationSaveServiceDto.class));
    }

    @Test
    void createReservation_redirectsWithSafeFailureMessage() throws Exception {
        Mockito.when(reservationService.create(Mockito.any(ReservationSaveServiceDto.class)))
                .thenThrow(new IllegalArgumentException("중복 예약은 불가합니다."));

        mockMvc.perform(post("/dashboard/reservations")
                        .param("name", "브라운")
                        .param("themeId", "1")
                        .param("timeId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "예약 생성에 실패했습니다. 입력값을 다시 확인해 주세요."));
    }

    @Test
    void deleteTheme_redirectsWithSafeFailureMessage() throws Exception {
        Mockito.doThrow(new ThemeNotFoundException(99L))
                .when(themeService).deleteById(99L);

        mockMvc.perform(post("/dashboard/themes/99/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/themes"))
                .andExpect(flash().attribute("errorMessage", "삭제할 테마를 찾지 못했습니다."));
    }

    @Test
    void deleteTime_redirectsWithSafeFailureMessage() throws Exception {
        Mockito.doThrow(new TimeNotFoundException(12L))
                .when(timeService).deleteById(12L);

        mockMvc.perform(post("/dashboard/times/12/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "삭제할 시간 슬롯을 찾지 못했습니다."));
    }

    @Test
    void cancelReservation_redirectsWithSafeFailureMessage() throws Exception {
        Mockito.doThrow(new ReservationNotFoundException(21L))
                .when(reservationService).cancel(21L);

        mockMvc.perform(post("/dashboard/reservations/21/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "취소할 예약을 찾지 못했습니다."));
    }

    @Test
    void deleteHoliday_redirectsWithSafeFailureMessage() throws Exception {
        Mockito.doThrow(new HolidayNotFoundException(7L))
                .when(holidayService).delete(7L);

        mockMvc.perform(post("/dashboard/holidays/7/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "삭제할 휴일을 찾지 못했습니다."));
    }
}
