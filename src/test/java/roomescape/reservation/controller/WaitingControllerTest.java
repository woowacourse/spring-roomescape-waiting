package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.reservation.domain.repository.dto.WaitingDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import roomescape.reservation.application.service.WaitingQueryService;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.presentation.controller.WaitingController;
import roomescape.reservation.presentation.dto.WaitingResponse;

@WebMvcTest(WaitingController.class)
class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingService waitingService;

    @MockitoBean
    private WaitingQueryService waitingQueryService;

    @DisplayName("이름으로 본인 대기 목록을 조회할 수 있다.")
    @Test
    void find_waitings_by_name() throws Exception {
        given(waitingQueryService.findAllByName("카야"))
                .willReturn(List.of(
                        WaitingResponse.from(new WaitingDetail(
                                1L, "카야", LocalDate.of(2026, 5, 27),
                                1L, "공포테마", "무서운 테마", "thumbnail1.jpg",
                                1L, LocalTime.of(10, 0), 1L
                        )),
                        WaitingResponse.from(new WaitingDetail(
                                2L, "카야", LocalDate.of(2026, 5, 28),
                                2L, "추리테마", "재미있는 추리 테마", "thumbnail2.jpg",
                                2L, LocalTime.of(14, 0), 2L
                        ))
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/waitings")
                        .param("name", "카야"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("카야"))
                .andExpect(jsonPath("$[0].date").value("2026-05-27"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("카야"))
                .andExpect(jsonPath("$[1].date").value("2026-05-28"));
    }

    @DisplayName("본인 대기를 취소할 수 있다.")
    @Test
    void cancel_waiting() throws Exception {
        given(waitingService.delete(any(), any()))
                .willReturn(1);

        mockMvc.perform(MockMvcRequestBuilders.delete("/waitings/1")
                        .param("name", "카야"))
                .andExpect(status().isNoContent());
    }
}
