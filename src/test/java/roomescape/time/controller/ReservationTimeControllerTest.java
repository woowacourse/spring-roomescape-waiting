package roomescape.time.controller;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.service.dto.AvailableTimesResult;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.dto.AvailableTimeQueryResult;
import roomescape.time.service.ReservationTimeService;

@WebMvcTest(ReservationTimeController.class)
@Import(WebMvcConfig.class)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("모든 예약 시간을 성공적으로 조회한다.")
    void getAllTimes_Success() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        given(reservationTimeService.findAll()).willReturn(List.of(time));

        // when & then
        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00:00"));
    }

    @Test
    @DisplayName("예약 가능한 시간을 성공적으로 조회한다.")
    void getAvailableTimes_Success() throws Exception {
        // given
        AvailableTimesResult result = new AvailableTimesResult(List.of(
                new AvailableTimeQueryResult(1L, LocalTime.of(10, 0))
        ));
        given(reservationTimeService.findAvailableTimes(any(), any())).willReturn(result);

        // when & then
        mockMvc.perform(get("/times/available-times")
                        .param("themeId", "1")
                        .param("date", "2026-05-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00:00"));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 시 파라미터가 누락되면 400 에러를 반환한다.")
    void getAvailableTimes_MissingParams_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/times/available-times")
                        .param("date", "2026-05-05")) // themeId missing
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 누락되었습니다."));
    }
}
