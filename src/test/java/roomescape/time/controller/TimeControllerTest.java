package roomescape.time.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.ReservationTimeConflictException;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.theme.service.ThemeService;
import roomescape.time.service.TimeService;

@WebMvcTest(TimeController.class)
class TimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeService timeService;

    @MockitoBean
    private ThemeService themeService;

    @DisplayName("날짜, 테마 ID로 예약 가능한 시간 목록을 조회한다.")
    @Test
    void 가능한_시간_목록_조회() throws Exception {
        Long themeId = 1L;
        LocalDate date = LocalDate.of(2026, 5, 6);

        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalDateTime.of(2026, 5, 6, 10, 0), LocalDateTime.of(2026, 5, 6, 12, 0)),
                new ReservationTime(2L, LocalDateTime.of(2026, 5, 6, 12, 0), LocalDateTime.of(2026, 5, 6, 14, 0))
        );
        Mockito.when(themeService.getAvailableTimes(themeId, date)).thenReturn(times);

        mockMvc.perform(get("/times")
                        .queryParam("themeId", String.valueOf(themeId))
                        .queryParam("date", "2026-05-06")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @DisplayName("전체 시간 목록을 조회한다.")
    @Test
    void 전체_시간_목록_조회() throws Exception {
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalDateTime.of(2030, 6, 1, 10, 0), LocalDateTime.of(2030, 6, 1, 12, 0)),
                new ReservationTime(2L, LocalDateTime.of(2030, 6, 1, 13, 0), LocalDateTime.of(2030, 6, 1, 15, 0))
        );
        Mockito.when(timeService.findAll()).thenReturn(times);

        mockMvc.perform(get("/times")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("2030-06-01T10:00"));
    }

    @DisplayName("시간 슬롯을 생성한다.")
    @Test
    void 시간_생성() throws Exception {
        ReservationTime saved = new ReservationTime(1L,
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));
        Mockito.when(timeService.create(Mockito.any(), Mockito.any()))
                .thenReturn(saved);

        String requestBody = """
                {
                    "startAt": "2030-06-01T10:00",
                    "endAt": "2030-06-01T12:00"
                }
                """;

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startAt").value("2030-06-01T10:00"))
                .andExpect(jsonPath("$.endAt").value("2030-06-01T12:00"));
    }

    @DisplayName("시간 슬롯을 삭제한다.")
    @Test
    void 시간_삭제() throws Exception {
        mockMvc.perform(delete("/times/{id}", 1L))
                .andExpect(status().isNoContent());

        Mockito.verify(timeService).deleteById(1L);
    }

    @DisplayName("존재하지 않는 시간 슬롯 삭제 요청인 경우, 404를 반환한다.")
    @Test
    void 존재하지_않는_시간_삭제_404() throws Exception {
        Mockito.doThrow(new TimeNotFoundException(999L))
                .when(timeService).deleteById(999L);

        mockMvc.perform(delete("/times/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @DisplayName("예약이 존재하는 시간 슬롯 삭제 요청인 경우, 409를 반환한다.")
    @Test
    void 예약이_있는_시간_삭제_409() throws Exception {
        Mockito.doThrow(new ReservationTimeConflictException(1L))
                .when(timeService).deleteById(1L);

        mockMvc.perform(delete("/times/{id}", 1L))
                .andExpect(status().isConflict());
    }

    @DisplayName("잘못된 날짜 형식으로 가능한 시간 조회 요청인 경우, 400을 반환한다.")
    @Test
    void 잘못된_날짜_형식_가능한_시간_조회_400() throws Exception {
        mockMvc.perform(get("/times")
                        .queryParam("themeId", "1")
                        .queryParam("date", "2026/05/06")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("startAt 없이 시간 생성 요청인 경우, 400을 반환한다.")
    @Test
    void startAt_누락_시간_생성_400() throws Exception {
        String requestBody = """
                {
                    "endAt": "2030-06-01T12:00"
                }
                """;

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("endAt 없이 시간 생성 요청인 경우, 400을 반환한다.")
    @Test
    void endAt_누락_시간_생성_400() throws Exception {
        String requestBody = """
                {
                    "startAt": "2030-06-01T10:00"
                }
                """;

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("잘못된 날짜시간 형식으로 시간 생성 요청인 경우, 400을 반환한다.")
    @Test
    void 잘못된_형식_시간_생성_400() throws Exception {
        String requestBody = """
                {
                    "startAt": "10시",
                    "endAt": "2030-06-01T12:00"
                }
                """;

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
