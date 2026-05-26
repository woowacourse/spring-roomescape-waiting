package roomescape.reservationtime.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.reservationtime.application.dto.AvailableReservationTimeQueryResult;
import roomescape.reservationtime.application.service.ReservationTimeService;
import roomescape.reservationtime.presentation.controller.ReservationTimeController;

@WebMvcTest(ReservationTimeController.class)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    void find_available_times() throws Exception {
        given(reservationTimeService.findAvailableTimes(eq(1L), eq(LocalDate.of(2028, 5, 4))))
                .willReturn(List.of(
                        new AvailableReservationTimeQueryResult(1L, LocalTime.of(9, 0), true),
                        new AvailableReservationTimeQueryResult(2L, LocalTime.of(10, 0), false)
                ));

        mockMvc.perform(get("/times")
                        .queryParam("themeId", "1")
                        .queryParam("date", "2028-05-04"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("09:00"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].startAt").value("10:00"))
                .andExpect(jsonPath("$[1].available").value(false));
    }

    @Test
    void find_available_times_with_invalid_date_format() throws Exception {
        mockMvc.perform(get("/times")
                        .queryParam("themeId", "1")
                        .queryParam("date", "2028/05/04"))
                .andExpect(status().isBadRequest());
    }
}
