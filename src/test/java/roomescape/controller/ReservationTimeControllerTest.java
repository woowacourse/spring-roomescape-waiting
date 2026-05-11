package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import roomescape.controller.dto.AvailableTimeListResponse;
import roomescape.controller.dto.AvailableTimeResponse;
import roomescape.controller.dto.ReservationTimeListResponse;
import roomescape.controller.dto.ReservationTimeResponse;
import roomescape.domain.ReservationTime;
import roomescape.repository.dto.ReservationTimeAvailability;
import roomescape.service.ReservationTimeService;

import java.time.LocalDate;
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
    public void getReservationTimeList() throws Exception {
        // given
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(12, 0)),
                new ReservationTime(3L, LocalTime.of(14, 0))
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

    @Test
    @DisplayName("예약 가능한 시간 목록을 조회한다.")
    public void getAvailableTimes() throws Exception {
        // given
        LocalDate date = LocalDate.of(2023, 8, 5);
        Long themeId = 1L;
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(2L, LocalTime.of(12, 0));
        List<ReservationTimeAvailability> timeAvailabilities = List.of(
                ReservationTimeAvailability.available(time),
                ReservationTimeAvailability.unavailable(time2)
        );
        given(reservationTimeService.findAvailableTimes(date, themeId)).willReturn(timeAvailabilities);

        // when then
        MvcResult result = mockMvc.perform(get("/times/availability")
                        .param("date", "2023-08-05")
                        .param("themeId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        AvailableTimeListResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AvailableTimeListResponse.class
        );

        assertThat(response.availableTimes())
                .extracting(
                        AvailableTimeResponse::id,
                        AvailableTimeResponse::startAt,
                        AvailableTimeResponse::isAvailable
                )
                .containsExactly(
                        tuple(1L, LocalTime.of(10, 0), true),
                        tuple(2L, LocalTime.of(12, 0), false)
                );

        then(reservationTimeService)
                .should()
                .findAvailableTimes(date, themeId);
    }

    @Test
    @DisplayName("예약 가능한 시간 목록을 조회할 때 날짜 형식이 올바르지 않으면 에러가 발생한다.")
    public void getAvailableTimes_fail_invalidDateFormat() throws Exception {
        // when then
        mockMvc.perform(get("/times/availability")
                        .param("date", "2023/08/05")
                        .param("themeId", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
