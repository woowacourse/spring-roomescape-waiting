package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationTimeResponseDTO;
import roomescape.dto.ThemeResponseDTO;
import roomescape.dto.WaitingRequestDTO;
import roomescape.dto.WaitingResponseDTO;
import roomescape.service.WaitingService;

@WebMvcTest(WaitingController.class)
class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WaitingService waitingService;

    @Test
    void 예약_대기를_생성하면_204_created와_Location헤더를_반환한다() throws Exception {
        WaitingRequestDTO request = new WaitingRequestDTO(
                "나무",
                LocalDate.parse("2026-05-28"),
                1L,
                1L
        );
        WaitingResponseDTO expectedResponse = new WaitingResponseDTO(
                1L,
                "나무",
                LocalDate.parse("2026-05-28"),
                ReservationTimeResponseDTO.from(ReservationTime.of(1L, LocalTime.parse("10:00"))),
                ThemeResponseDTO.from(Theme.of(1L, "귀신 찾기", "귀신을 찾습니다.", "example.com")),
                1L
        );

        given(waitingService.addWaiting(request)).willReturn(expectedResponse);

        mockMvc.perform(
                        post("/api/waiting")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                ).andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/waiting/1"));
    }

    @Test
    void 예약_대기를_삭제하면_204_noContent를_반환한다() throws Exception {
        Long waitingId = 1L;

        doNothing().when(waitingService).deleteWaiting(waitingId);

        mockMvc.perform(
                delete("/api/waiting/{id}", waitingId)
        ).andExpect(status().isNoContent());
    }
}
