package roomescape.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.ThemeService;
import roomescape.service.dto.AvailableTimes;
import roomescape.service.dto.PopularTheme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void 테마_목록을_조회한다() throws Exception {
        List<Theme> themes = List.of(
                new Theme(1L, "공포방", "무서운방입니다.", "image-url"),
                new Theme(2L, "추리방", "추리하는방입니다.", "image-url2")
        );
        when(themeService.findThemes()).thenReturn(themes);

        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themes.length()").value(2))
                .andExpect(jsonPath("$.themes[0].name").value("공포방"))
                .andExpect(jsonPath("$.themes[1].name").value("추리방"));
    }

    @Test
    void 특정테마와_날짜를_통해_예약_가능한_시간을_조회한다() throws Exception {
        List<ReservationTime> reservationTimes = List.of(
                new ReservationTime(1L, LocalTime.of(13, 0)),
                new ReservationTime(2L, LocalTime.of(15, 0)),
                new ReservationTime(3L, LocalTime.of(17, 0)));
        AvailableTimes availableTimes = AvailableTimes.from(reservationTimes, List.of(2L));
        when(themeService.findAvailableTimes(anyLong(), any())).thenReturn(availableTimes);

        mockMvc.perform(get("/themes/1/available-times")
                        .param("date", "2025-05-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.times.length()").value(3))
                .andExpect(jsonPath("$.times[0].id").value(1L))
                .andExpect(jsonPath("$.times[1].startAt").value("15:00"))
                .andExpect(jsonPath("$.times[2].isAvailable").value(true));
    }

    @Test
    void 인기테마_목록을_조회한다() throws Exception {
        List<PopularTheme> popularThemes = List.of(
                new PopularTheme(1L, "레서방1", "레서방입니다1.", "path/to/image1", 1),
                new PopularTheme(2L, "레서방2", "레서방입니다2.", "path/to/image2", 2),
                new PopularTheme(3L, "레서방3", "레서방입니다3.", "path/to/image3", 3),
                new PopularTheme(4L, "레서방4", "레서방입니다4.", "path/to/image4", 4),
                new PopularTheme(5L, "레서방5", "레서방입니다5.", "path/to/image5", 5),
                new PopularTheme(6L, "레서방6", "레서방입니다6.", "path/to/image6", 6),
                new PopularTheme(7L, "레서방7", "레서방입니다7.", "path/to/image7", 7),
                new PopularTheme(8L, "레서방8", "레서방입니다8.", "path/to/image8", 8),
                new PopularTheme(9L, "레서방9", "레서방입니다9.", "path/to/image9", 9),
                new PopularTheme(10L, "레서방10", "레서방입니다10.", "path/to/image10", 10)
        );
        when(themeService.findPopularThemes(any(LocalDate.class), anyInt(), anyInt())).thenReturn(popularThemes);

        mockMvc.perform(get("/themes/popular")
                        .param("days", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themes.length()").value(10));
    }
}
