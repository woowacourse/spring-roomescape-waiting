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
import roomescape.global.exception.NotFoundException;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.dto.AvailableTimeQueryResult;
import roomescape.time.service.ReservationTimeQueryService;
import roomescape.time.service.dto.AvailableTimesResult;
import roomescape.time.service.dto.ReservationTimeResult;

@WebMvcTest(ReservationTimeController.class)
@Import(WebMvcConfig.class)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeQueryService reservationTimeQueryService;

    @Test
    @DisplayName("모든 예약 시간을 성공적으로 조회한다.")
    void readAll_Success() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        given(reservationTimeQueryService.findAll()).willReturn(List.of(ReservationTimeResult.from(time)));

        // when & then
        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00:00"));
    }

    @Test
    @DisplayName("예약 가능한 시간을 성공적으로 조회한다.")
    void readAvailable_Success() throws Exception {
        // given
        AvailableTimesResult result = new AvailableTimesResult(List.of(
                new AvailableTimeQueryResult(1L, LocalTime.of(10, 0), false)
        ));
        given(reservationTimeQueryService.queryAvailableTimes(any(), any())).willReturn(result);

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
    void readAvailable_MissingParams_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/times/available-times")
                        .param("date", "2026-05-05")) // themeId missing
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 누락되었습니다. 입력 값을 다시 확인해 주세요."));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 시 테마가 존재하지 않으면 404 에러를 반환한다.")
    void readAvailable_ThemeNotFound_NotFound() throws Exception {
        // given
        given(reservationTimeQueryService.queryAvailableTimes(any(), any()))
                .willThrow(new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage()));

        // when & then
        mockMvc.perform(get("/times/available-times")
                        .param("themeId", "9999")
                        .param("date", "2026-05-05"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ThemeErrorCode.THEME_NOT_FOUND.getMessage()));
    }
}
