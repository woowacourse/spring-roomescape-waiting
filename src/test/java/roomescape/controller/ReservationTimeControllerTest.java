package roomescape.controller;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationTime;
import roomescape.dto.reservationtime.response.ReservationTimeResponses;
import roomescape.infrastructure.AuthInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.ReservationTimeService;

@WebMvcTest(controllers = ReservationTimeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, AuthInterceptor.class, LoginUserArgumentResolver.class}))
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("GET /times - 목록과 hasNext를 응답한다")
    void getReservationTimesRespondsWithListAndHasNext() throws Exception {
        given(reservationTimeService.getReservationTimes(0, 20))
                .willReturn(ReservationTimeResponses.of(
                        List.of(new ReservationTime(1L, LocalTime.of(10, 0))), false));

        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.times.size()").value(1))
                .andExpect(jsonPath("$.times[0].startAt").value("10:00"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /times/{id} - 단건을 응답한다")
    void getReservationTimeRespondsWithSingle() throws Exception {
        given(reservationTimeService.getReservationTime(1L))
                .willReturn(new ReservationTime(1L, LocalTime.of(10, 0)));

        mockMvc.perform(get("/times/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startAt").value("10:00"));
    }

    @Test
    @DisplayName("GET /times/{id} - 서비스가 ResourceNotFoundException을 던지면 404과 메시지를 반환한다")
    void getReservationTimeReturns404OnResourceNotFoundException() throws Exception {
        given(reservationTimeService.getReservationTime(9999L))
                .willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약 시간을(를) 찾을 수 없습니다. id=9999"));

        mockMvc.perform(get("/times/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
