package roomescape.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationTime;
import roomescape.service.ReservationTimeService;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationTimeController.class)
class ReservationTimeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    void 시간_목록_조회_요청을_Service에_전달하고_결과를_반환한다() throws Exception {
        List<ReservationTime> created = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0)),
                new ReservationTime(3L, LocalTime.of(12, 0)),
                new ReservationTime(4L, LocalTime.of(13, 0))
        );

        when(reservationTimeService.findTimes()).thenReturn(created);

        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.times.length()").value(4))
                .andExpect(jsonPath("$.times[0].id").value(1L))
                .andExpect(jsonPath("$.times[1].startAt").value("11:00"));
    }
}
