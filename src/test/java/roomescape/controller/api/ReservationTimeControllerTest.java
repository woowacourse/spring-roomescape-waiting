package roomescape.controller.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationTimeStatus;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.TimeSlotResponse;
import roomescape.service.ReservationTimeService;

@WebMvcTest(ReservationTimeController.class)
public class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    void 전체_시간_조회() throws Exception {
        ReservationTimeResponse response = new ReservationTimeResponse(1L, LocalTime.of(10, 0));
        given(reservationTimeService.findAll()).willReturn(List.of(response));

        mockMvc.perform(get("/api/times"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00:00"));
    }

    @Test
    void 예약_가능_시간_조회() throws Exception {
        TimeSlotResponse response = new TimeSlotResponse(1L, LocalTime.of(10, 0), ReservationTimeStatus.AVAILABLE);
        given(reservationTimeService.findAvailableTime(1L, LocalDate.now())).willReturn(List.of(response));

        mockMvc.perform(get("/api/times/available-times")
                        .param("themeId", "1")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00:00"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }
}
