package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import roomescape.dto.response.ReservationOrderResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationMineController.class)
class ReservationMineControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void 내_예약_및_대기_목록_조회_API() throws Exception {
        String name = "아나키";
        ReservationOrderResponse confirmed = new ReservationOrderResponse(
                1L, name, LocalDate.now().plusDays(1),
                new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                new ThemeResponse(1L, "테마1", "설명", "썸네일"),
                0L, "예약"
        );
        ReservationOrderResponse waiting = new ReservationOrderResponse(
                2L, name, LocalDate.now().plusDays(2),
                new ReservationTimeResponse(2L, LocalTime.of(11, 0)),
                new ThemeResponse(2L, "테마2", "설명", "썸네일"),
                2L, "2번째 예약대기"
        );

        given(reservationService.findByName(name)).willReturn(List.of(confirmed, waiting));

        mockMvc.perform(get("/reservations-mine").queryParam("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("예약"))
                .andExpect(jsonPath("$[1].status").value("2번째 예약대기"));
    }
}
