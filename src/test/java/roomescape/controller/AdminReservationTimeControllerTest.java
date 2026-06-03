package roomescape.controller;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationTime;
import roomescape.dto.reservationtime.command.CreateReservationTimeCommand;
import roomescape.infrastructure.AuthInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.ReservationTimeService;

@WebMvcTest(controllers = AdminReservationTimeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, AuthInterceptor.class, LoginUserArgumentResolver.class}))
class AdminReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("POST /admin/times - 생성된 id를 Location 헤더에 담아 201을 반환한다")
    void createReservationTimeReturns201WithLocationHeader() throws Exception {
        given(reservationTimeService.createReservationTime(any(CreateReservationTimeCommand.class)))
                .willReturn(new ReservationTime(5L, LocalTime.of(10, 0)));

        Map<String, Object> body = Map.of("startAt", "10:00");

        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/times/5"));
    }

    @Test
    @DisplayName("DELETE /admin/times/{id} - 200을 반환하고 서비스에 위임한다")
    void deleteReservationTimeReturns200AndDelegates() throws Exception {
        mockMvc.perform(delete("/admin/times/3"))
                .andExpect(status().isOk());

        verify(reservationTimeService).deleteReservationTime(3L);
    }

    @Test
    @DisplayName("POST /admin/times - 본문의 startAt이 시간 형식이 아니면 400과 메시지를 반환한다")
    void createReservationTimeReturns400WhenStartAtIsNotTimeFormat() throws Exception {
        String body = """
                {"startAt":"abc"}
                """;

        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("POST /admin/times - 본문의 startAt이 누락되면 400과 메시지를 반환한다")
    void createReservationTimeReturns400WhenStartAtIsMissing() throws Exception {
        String body = "{}";

        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("DELETE /admin/times - 서비스가 ResourceNotFoundException을 던지면 404과 메시지를 반환한다")
    void deleteReservationTimeReturns404OnResourceNotFoundException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약 시간", 9999L))
                .given(reservationTimeService).deleteReservationTime(9999L);

        mockMvc.perform(delete("/admin/times/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /admin/times - 서비스가 ReservationTimeInUseException을 던지면 409과 메시지를 반환한다")
    void deleteReservationTimeReturns409OnReservationTimeInUseException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESERVATION_TIME_IN_USE))
                .given(reservationTimeService).deleteReservationTime(3L);

        mockMvc.perform(delete("/admin/times/3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_TIME_IN_USE"));
    }
}
