package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import roomescape.reservation.application.dto.WaitingQueryResult;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.presentation.controller.WaitingController;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;
import roomescape.theme.application.dto.ThemeQueryResult;

@WebMvcTest(WaitingController.class)
class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingService waitingService;

    @Test
    void findAll() throws Exception {
        given(waitingService.findAllByName(any()))
                .willReturn(List.of(
                        new WaitingQueryResult(
                                1L,
                                "카야",
                                LocalDate.of(2026, 5, 27),
                                new ThemeQueryResult(
                                        1L,
                                        "공포테마",
                                        "무서운 테마",
                                        "thumbnail1.jpg"
                                ),
                                new ReservationTimeQueryResult(
                                        1L,
                                        LocalTime.of(10, 0)
                                ),
                                1L
                        ),
                        new WaitingQueryResult(
                                2L,
                                "카야",
                                LocalDate.of(2026, 5, 28),
                                new ThemeQueryResult(
                                        2L,
                                        "추리테마",
                                        "재미있는 추리 테마",
                                        "thumbnail2.jpg"
                                ),
                                new ReservationTimeQueryResult(
                                        2L,
                                        LocalTime.of(14, 0)
                                ),
                                2L
                        )
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/waitings")
                        .param("name", "카야"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("카야"))
                .andExpect(jsonPath("$[0].date").value("2026-05-27"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("카야"))
                .andExpect(jsonPath("$[1].date").value("2026-05-28"));
    }

    @Test
    void delete() throws Exception {
        given(waitingService.delete(any(), any()))
                .willReturn(1);

        mockMvc.perform(MockMvcRequestBuilders.delete("/waitings/1")
                        .param("name", "카야"))
                .andExpect(status().isNoContent());
    }
}