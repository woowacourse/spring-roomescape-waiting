package roomescape.domain.reservationtime;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.reservationtime.dto.ReservationTimeAvailabilityResponse;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ThemeErrors;

@WebMvcTest(ReservationTimeController.class)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 가능 시간 조회의 요청과 응답을 확인한다.")
    void getReservationTimeAvailability() throws Exception {
        // given
        Long themeId = 1L;
        Long dateId = 2L;
        ReservationTimeAvailabilityResponse response = new ReservationTimeAvailabilityResponse(
            3L,
            LocalTime.of(10, 10),
            true
        );
        given(reservationTimeService.getReservationTimeAvailability(themeId, dateId))
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/reservation-times/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .param("themeId", String.valueOf(themeId))
                .param("dateId", String.valueOf(dateId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].timeId").value(3))
            .andExpect(jsonPath("$[0].startAt").value("10:10"))
            .andExpect(jsonPath("$[0].available").value(true));

        verify(reservationTimeService).getReservationTimeAvailability(themeId, dateId);
    }

    @Test
    @DisplayName("예약 가능 시간 조회 시 themeId가 누락되면 예외가 발생한다.")
    void getReservationTimeAvailabilityWithoutThemeId() throws Exception {
        // given & when & then
        mockMvc.perform(get("/reservation-times/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .param("dateId", "2"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REQUIRED_PARAMETER_MISSING"))
            .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 누락되었습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약 가능 시간 조회 시 예외가 발생한다.")
    void getReservationTimeAvailabilityWhenThemeNotFound() throws Exception {
        // given
        Long themeId = 999L;
        Long dateId = 2L;
        given(reservationTimeService.getReservationTimeAvailability(themeId, dateId))
            .willThrow(new NotFoundException(ThemeErrors.THEME_NOT_EXIST));

        // when & then
        mockMvc.perform(get("/reservation-times/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .param("themeId", String.valueOf(themeId))
                .param("dateId", String.valueOf(dateId)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("THEME_NOT_EXIST"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 테마 입니다."));
    }
}
