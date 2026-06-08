package roomescape.reservationtime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import roomescape.reservationtime.controller.dto.ReservationTimeListResponse;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReservationTimeController.class)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 시간 목록을 조회한다.")
    void getReservationTimeList() throws Exception {
        // given
        List<ReservationTime> times = List.of(
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                ReservationTime.of(2L, LocalTime.of(12, 0)),
                ReservationTime.of(3L, LocalTime.of(14, 0))
        );
        given(reservationTimeService.findAllReservationTimes()).willReturn(times);

        // when then
        MvcResult result = mockMvc.perform(get("/times"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ReservationTimeListResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ReservationTimeListResponse.class
        );

        assertTimeResponse(response);

        then(reservationTimeService)
                .should()
                .findAllReservationTimes();
    }

    private static void assertTimeResponse(ReservationTimeListResponse response) {
        assertThat(response.times()).hasSize(3)
                .extracting(
                        ReservationTimeResponse::id,
                        ReservationTimeResponse::startAt
                )
                .containsExactly(
                        tuple(1L, "10:00"),
                        tuple(2L, "12:00"),
                        tuple(3L, "14:00")
                );
    }

}
