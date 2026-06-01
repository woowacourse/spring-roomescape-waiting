package roomescape.time.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.theme.service.dto.AvailableTimesResult;
import roomescape.time.repository.dto.AvailableTimeQueryResult;
import roomescape.time.service.ReservationTimeService;

@WebMvcTest(ReservationTimeController.class)
class ReservationTimeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReservationTimeService reservationTimeService;

    @DisplayName("예약 가능 시간 목록 조회 시, 테마 id와 날짜를 입력받아, 200을 반환한다.")
    @Test
    void getAvailableTimes_success() throws Exception {
        //given
        when(reservationTimeService.findAvailableReservationTimes(any(), any()))
                .thenReturn(new AvailableTimesResult(List.of(
                        new AvailableTimeQueryResult(1L, LocalTime.of(10, 0))
                )));

        //when& then
        mockMvc.perform(
                get("/times/available-times")
                        .param("themeId", "1")
                        .param("date", "2026-06-01")
        ).andExpect(status().isOk());
    }

    @DisplayName("예약 가능 시간 목록 조회 시에, themeId에 값이 없으면 400을 반환한다.")
    @Test
    void getAvailableTimes_no_themeId() throws Exception {
        mockMvc.perform(
                        get("/times/available-times")
                                .param("date", "2026-06-01")
                ).andExpect(status().isBadRequest());
    }

    @DisplayName("예약 가능 시간 목록 조회 시에, themeId가 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void getAvailableTimes_invalid_themeId() throws Exception {
        //given
        when(reservationTimeService.findAvailableReservationTimes(any(), any()))
                .thenReturn(new AvailableTimesResult(List.of(
                        new AvailableTimeQueryResult(1L, LocalTime.of(10, 0))
                )));

        //when& then
        mockMvc.perform(
                get("/times/available-times")
                        .param("themeId", "invalid")
                        .param("date", "2026-06-01")
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("예약 가능 시간 목록 조회 시에, date에 값이 없으면 400을 반환한다.")
    @Test
    void getAvailableTimes_no_date() throws Exception {
        mockMvc.perform(
                get("/times/available-times")
                        .param("themeId", "1")
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("예약 가능 시간 목록 조회 시에, date 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void getAvailableTimes_invalid_date() throws Exception {
        //given
        when(reservationTimeService.findAvailableReservationTimes(any(), any()))
                .thenReturn(new AvailableTimesResult(List.of(
                        new AvailableTimeQueryResult(1L, LocalTime.of(10, 0))
                )));

        //when& then
        mockMvc.perform(
                get("/times/available-times")
                        .param("themeId", "1")
                        .param("date", "invalid")
        ).andExpect(status().isBadRequest());
    }
}
