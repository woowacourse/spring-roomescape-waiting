package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.ProblemDetailsAdvice;
import roomescape.service.SessionService;

@WebMvcTest(WaitingController.class)
@Import(ProblemDetailsAdvice.class)
class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SessionService sessionService;

    @Test
    @DisplayName("유효한 요청으로 예약 대기를 신청하고 200 상태 코드를 반환한다.")
    void applyWaiting() throws Exception {
        given(sessionService.addWaiting(any())).willReturn(createMockWaiting());
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new WaitingRequest("브라운", LocalDate.now().plusDays(1), 1L, 1L, 0L))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약 대기를 취소하고 204 상태 코드를 반환한다.")
    void cancelWaiting() throws Exception {
        doNothing().when(sessionService).cancelWaiting(anyLong(), anyString());
        mockMvc.perform(delete("/waitings/1").param("userName", "브라운"))
                .andExpect(status().isNoContent());
    }

    private Waiting createMockWaiting() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "https://url");
        Session session = new Session(1L, LocalDate.now().plusDays(1), timeSlot, theme);
        return new Waiting(1L, "브라운", session, 1);
    }
}
