package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import roomescape.controller.dto.SessionRequest;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.ProblemDetailsAdvice;
import roomescape.service.SessionService;

@WebMvcTest(SessionController.class)
@Import(ProblemDetailsAdvice.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SessionService sessionService;

    @Test
    @DisplayName("전체 세션 목록을 조회하고 200 상태 코드를 반환한다.")
    void getSessions() throws Exception {
        given(sessionService.allSessions()).willReturn(List.of(createMockSession()));
        mockMvc.perform(get("/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("유효한 데이터로 세션을 생성하고 201 상태 코드와 Location 헤더를 반환한다.")
    void createSession() throws Exception {
        given(sessionService.createSession(any(), anyLong(), anyLong())).willReturn(createMockSession());
        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SessionRequest(LocalDate.now().plusDays(1), 1L, 1L))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("날짜에 해당하는 모든 세션을 일괄 생성하고 200 상태 코드를 반환한다.")
    void createSessionsForDate() throws Exception {
        given(sessionService.createSessionsForDate(any())).willReturn(List.of(createMockSession()));
        mockMvc.perform(post("/sessions/batch")
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private Session createMockSession() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "https://url");
        return new Session(1L, LocalDate.now().plusDays(1), timeSlot, theme);
    }
}
