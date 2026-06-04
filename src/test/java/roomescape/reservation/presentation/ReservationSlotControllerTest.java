package roomescape.reservation.presentation;

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
import roomescape.reservation.application.ReservationSlotService;
import roomescape.reservation.presentation.response.ReservationSlotResponse;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.errors.ThemeErrors;

@WebMvcTest(ReservationSlotController.class)
class ReservationSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationSlotService reservationSlotService;

    @Test
    @DisplayName("예약 슬롯 조회의 요청과 응답을 확인한다.")
    void getReservationSlots() throws Exception {
        // given
        Long themeId = 1L;
        Long dateId = 2L;
        ReservationSlotResponse response = new ReservationSlotResponse(
            3L,
            LocalTime.of(10, 10),
            2L
        );
        given(reservationSlotService.getReservationSlots(themeId, dateId))
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/reservation-slots")
                .contentType(MediaType.APPLICATION_JSON)
                .param("themeId", String.valueOf(themeId))
                .param("dateId", String.valueOf(dateId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].timeId").value(3))
            .andExpect(jsonPath("$[0].startAt").value("10:10"))
            .andExpect(jsonPath("$[0].waitingNumber").value(2));

        verify(reservationSlotService).getReservationSlots(themeId, dateId);
    }

    @Test
    @DisplayName("예약 슬롯 조회 시 themeId가 누락되면 예외가 발생한다.")
    void getReservationSlotsWithoutThemeId() throws Exception {
        // given & when & then
        mockMvc.perform(get("/reservation-slots")
                .contentType(MediaType.APPLICATION_JSON)
                .param("dateId", "2"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REQUIRED_PARAMETER_MISSING"))
            .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 누락되었습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약 슬롯 조회 시 예외가 발생한다.")
    void getReservationSlotsWhenThemeNotFound() throws Exception {
        // given
        Long themeId = 999L;
        Long dateId = 2L;
        given(reservationSlotService.getReservationSlots(themeId, dateId))
            .willThrow(new NotFoundException(ThemeErrors.THEME_NOT_EXIST));

        // when & then
        mockMvc.perform(get("/reservation-slots")
                .contentType(MediaType.APPLICATION_JSON)
                .param("themeId", String.valueOf(themeId))
                .param("dateId", String.valueOf(dateId)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("THEME_NOT_EXIST"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 테마 입니다."));
    }
}
