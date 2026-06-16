package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.domain.Theme;
import roomescape.global.exception.DomainErrorHttpMapper;
import roomescape.service.AuthService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;
import roomescape.service.dto.AvailableTimeResult;

@WebMvcTest(UserThemeController.class)
@Import(DomainErrorHttpMapper.class)
class UserThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private AuthService authService;

    @DisplayName("전체 테마 목록을 조회한다.")
    @Test
    void findAll() throws Exception {
        given(themeService.findAll()).willReturn(List.of(
                new Theme(1L, "잠긴 방", "설명", "https://example.com/theme.jpg")
        ));

        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("잠긴 방"))
                .andExpect(jsonPath("$[0].thumbnailUrl").value("https://example.com/theme.jpg"));
    }

    @DisplayName("인기 테마 목록을 조회한다.")
    @Test
    void findPopularThemes() throws Exception {
        given(themeService.findPopularThemes()).willReturn(List.of(
                new Theme(1L, "인기 테마", "설명", "https://example.com/theme.jpg")
        ));

        mockMvc.perform(get("/themes/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("인기 테마"));
    }

    @DisplayName("특정 테마의 이용 가능 시간을 조회한다.")
    @Test
    void findAvailableTimes() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 1);
        given(reservationTimeService.findAvailableTimes(1L, date)).willReturn(List.of(
                new AvailableTimeResult(1L, LocalTime.of(10, 0), 0),
                new AvailableTimeResult(2L, LocalTime.of(11, 0), 2)
        ));

        mockMvc.perform(get("/themes/1/available-times").param("date", "2026-07-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startAt").value("10:00"))
                .andExpect(jsonPath("$[0].isAvailable").value(true))
                .andExpect(jsonPath("$[1].startAt").value("11:00"))
                .andExpect(jsonPath("$[1].waitNumber").value(1));
    }

    @DisplayName("이용 가능 시간 조회의 날짜 형식이 잘못되면 400을 반환한다.")
    @Test
    void findAvailableTimesInvalidDate() throws Exception {
        mockMvc.perform(get("/themes/1/available-times").param("date", "2026-99-99"))
                .andExpect(status().isBadRequest());
    }
}
