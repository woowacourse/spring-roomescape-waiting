package roomescape.time.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.member.security.service.MemberAuthService;
import roomescape.member.service.MemberService;
import roomescape.time.domain.Time;
import roomescape.time.dto.TimeRequest;
import roomescape.time.dto.TimeResponse;
import roomescape.time.service.TimeService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TimeController.class)
class TimeControllerTest {

    private final Time time = new Time(4L, LocalTime.of(12,0));

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeService timeService;
    @MockBean
    private MemberService memberService;
    @MockBean
    private MemberAuthService memberAuthService;

    @Test
    @DisplayName("시간을 잘 저장하는지 확인한다.")
    void createReservationTime() throws Exception {
        Mockito.when(timeService.addReservationTime(any()))
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
                .andExpect(status().isCreated());
    }

    public TimeResponse toResponse(Time time) {
        return new TimeResponse(time.getId(), time.getStartAt());
    }

    @Test
    @DisplayName("시간 정보를 잘 불러오는지 확인한다.")
    void reservationTimesList() throws Exception {
        Mockito.when(timeService.findReservationTimes())
                .thenReturn(List.of(toResponse(time)));

        mockMvc.perform(get("/times"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약 정보를 잘 지우는지 확인한다.")
    void deleteReservationTime() throws Exception {
        mockMvc.perform(delete("/times/4"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

}
