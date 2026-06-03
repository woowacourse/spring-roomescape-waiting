package roomescape.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.service.WaitingService;
import roomescape.service.dto.WaitingResult;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WaitingController.class)
class WaitingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private WaitingService waitingService;

    @Test
    void 예약_대기_생성_요청을_받으면_DTO의_정보를_Service에_전달하고_결과를_반환한다() throws Exception {
        Waiting waiting = new Waiting(1L, "레서", new Schedule(LocalDate.of(2026, 5, 6),
                new ReservationTime(1L, LocalTime.of(18, 0)),
                new Theme(1L, "공포방", "무서운방입니다.", "image-url")));
        when(waitingService.createWaiting(any(), any(), anyLong(), anyLong()))
                .thenReturn(WaitingResult.of(waiting, 2L));

        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {
                                    "name": "레서",
                                    "date": "2026-05-06",
                                    "timeId": 1,
                                    "themeId": 1
                                  }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("레서"))
                .andExpect(jsonPath("$.date").value("2026-05-06"))
                .andExpect(jsonPath("$.time.id").value(1))
                .andExpect(jsonPath("$.time.startAt").value("18:00"))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.theme.name").value("공포방"))
                .andExpect(jsonPath("$.order").value(2));

        verify(waitingService).createWaiting("레서", LocalDate.of(2026, 5, 6), 1L, 1L);
    }

    @Test
    void 예약_대기_삭제_요청을_받으면_PathVariable_id와_이름을_Service에_전달한다() throws Exception {
        mockMvc.perform(delete("/waitings/1")
                        .param("name", "레서"))
                .andExpect(status().isNoContent());

        verify(waitingService, times(1)).deleteWaiting(1L, "레서");
    }
}
