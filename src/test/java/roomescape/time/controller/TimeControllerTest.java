package roomescape.time.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.model.ControllerTest;
import roomescape.time.domain.Time;
import roomescape.time.dto.TimeRequest;
import roomescape.time.dto.TimeResponse;
import roomescape.time.service.TimeService;

@WebMvcTest(TimeController.class)
class TimeControllerTest extends ControllerTest {
    private final Time time = new Time(4L, LocalTime.of(10, 0, 0));
    private final String expectedTime = "10:00:00";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeService timeService;

    @Test
    @DisplayName("시간을 잘 저장하는지 확인한다.")
    void reservationTimeSave() throws Exception {
        when(timeService.addReservationTime(any()))
                .thenReturn(toResponse(time));

        String content = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writeValueAsString(new TimeRequest(time.getStartAt()));

        mockMvc.perform(post("/times")
                        .content(content)
                        .contentType("application/Json")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(time.getId()))
                .andExpect(jsonPath("$.startAt").value(expectedTime));
    }

    @Test
    @DisplayName("시간 정보를 잘 불러오는지 확인한다.")
    void reservationTimesList() throws Exception {
        when(timeService.findReservationTimes())
                .thenReturn(List.of(toResponse(time)));

        mockMvc.perform(get("/times"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(time.getId()))
                .andExpect(jsonPath("$[0].startAt").value(expectedTime));
    }

    @Test
    @DisplayName("예약 정보를 잘 지우는지 확인한다.")
    void reservationTimeRemove() throws Exception {
        mockMvc.perform(delete("/times/4"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    public TimeResponse toResponse(Time time) {
        return new TimeResponse(time.getId(), time.getStartAt());
    }
}
