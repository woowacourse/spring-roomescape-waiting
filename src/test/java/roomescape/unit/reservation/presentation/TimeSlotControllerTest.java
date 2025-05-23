package roomescape.unit.reservation.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.auth.presentation.AuthorizationExtractor;
import roomescape.reservation.dto.request.TimeSlotRequest;
import roomescape.reservation.dto.response.TimeSlotResponse;
import roomescape.reservation.presentation.TimeSlotController;
import roomescape.reservation.service.TimeSlotService;

@WebMvcTest(value = {TimeSlotController.class})
class TimeSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TimeSlotService timeSlotService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private AuthorizationExtractor authorizationExtractor;

    @Test
    void 예약시간을_전체_조회하는데_성공한다() throws Exception {
        // given
        TimeSlotResponse time = new TimeSlotResponse(1L, LocalTime.of(9, 0));
        List<TimeSlotResponse> response = List.of(time);
        given(timeSlotService.findAllTimes()).willReturn(response);
        // when & then
        mockMvc.perform(get("/api/times")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    void 예약시간을_추가하는데_성공한다() throws Exception {
        // given
        TimeSlotRequest request = new TimeSlotRequest(LocalTime.of(9, 0));
        TimeSlotResponse response = new TimeSlotResponse(1L, LocalTime.of(9, 0));
        given(timeSlotService.createTime(request)).willReturn(response);
        // when & then
        mockMvc.perform(post("/api/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/times/" + 1))
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }
}