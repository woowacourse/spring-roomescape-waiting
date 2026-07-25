package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.TimePatchRequest;
import roomescape.controller.dto.TimeRequest;
import roomescape.domain.TimeSlot;
import roomescape.exception.ProblemDetailsAdvice;
import roomescape.service.SessionService;
import roomescape.service.TimeSlotService;
import roomescape.service.dto.AvailableTimeSlot;

@WebMvcTest(TimeController.class)
@Import(ProblemDetailsAdvice.class)
class TimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TimeSlotService timeSlotService;

    @MockitoBean
    private SessionService sessionService;

    @Test
    @DisplayName("전체 예약 시간 목록을 조회하고 200 상태 코드를 반환한다.")
    void getTimes() throws Exception {
        given(timeSlotService.allTimes()).willReturn(List.of(createMockTimeSlot()));
        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("식별자로 단건 예약 시간을 조회하고 200 상태 코드를 반환한다.")
    void getTimeById() throws Exception {
        given(timeSlotService.findTimeSlotById(anyLong())).willReturn(createMockTimeSlot());
        mockMvc.perform(get("/times/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("테마와 날짜로 이용 가능한 시간 목록을 조회하고 200 상태 코드를 반환한다.")
    void getAvailableTimes() throws Exception {
        AvailableTimeSlot availableSlot = new AvailableTimeSlot(createMockTimeSlot(), true);
        given(sessionService.findAvailableTimes(anyLong(), any(LocalDate.class))).willReturn(List.of(availableSlot));
        mockMvc.perform(get("/times").param("themeId", "1").param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("유효한 데이터로 예약 시간을 생성하고 201 상태 코드와 Location 헤더를 반환한다.")
    void createTime() throws Exception {
        given(timeSlotService.saveTime(any())).willReturn(createMockTimeSlot());
        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimeRequest(LocalTime.of(10, 0)))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("예약 시간을 삭제하고 204 상태 코드를 반환한다.")
    void deleteTime() throws Exception {
        doNothing().when(timeSlotService).removeTime(anyLong());
        mockMvc.perform(delete("/times/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("예약 시간의 전체 정보를 수정(PUT)하고 200 상태 코드를 반환한다.")
    void updateTime() throws Exception {
        given(timeSlotService.putTime(anyLong(), any())).willReturn(createMockTimeSlot());
        mockMvc.perform(put("/times/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimeRequest(LocalTime.of(14, 0)))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약 시간의 일부 정보를 수정(PATCH)하고 200 상태 코드를 반환한다.")
    void patchTime() throws Exception {
        given(timeSlotService.patchTime(anyLong(), any())).willReturn(createMockTimeSlot());
        mockMvc.perform(patch("/times/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimePatchRequest(LocalTime.of(16, 0)))))
                .andExpect(status().isOk());
    }

    private TimeSlot createMockTimeSlot() {
        return new TimeSlot(1L, LocalTime.of(10, 0));
    }
}
