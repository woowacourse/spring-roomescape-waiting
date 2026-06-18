package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ReservationService;

@WebMvcTest(WaitingController.class)
class WaitingControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 예약_대기_신청_API() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        WaitingRequest request = new WaitingRequest("아나키", date, 1L, 1L);
        ReservationResponse response = new ReservationResponse(
                23L, "아나키", date,
                new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                new ThemeResponse(1L, "테마1", "설명", "썸네일")
        );

        given(reservationService.save(any(), any())).willReturn(response);

        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("아나키"));
    }

    @Test
    void 예약_대기_취소_API() throws Exception {
        mockMvc.perform(delete("/waitings/1"))
                .andExpect(status().isNoContent());
    }
}
