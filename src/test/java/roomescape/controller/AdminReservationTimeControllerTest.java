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
    void POST_admin_times_생성된_id를_Location_헤더에_담아_201을_반환한다() throws Exception {
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
    void DELETE_admin_times_id_200을_반환하고_서비스에_위임한다() throws Exception {
        mockMvc.perform(delete("/admin/times/3"))
                .andExpect(status().isOk());

        verify(reservationTimeService).deleteReservationTime(3L);
    }

    @Test
    void POST_admin_times_본문의_startAt이_시간_형식이_아니면_400과_메시지를_반환한다() throws Exception {
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
    void POST_admin_times_본문의_startAt이_누락되면_400과_메시지를_반환한다() throws Exception {
        String body = "{}";

        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void DELETE_admin_times_서비스가_ResourceNotFoundException을_던지면_404과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약 시간", 9999L))
                .given(reservationTimeService).deleteReservationTime(9999L);

        mockMvc.perform(delete("/admin/times/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void DELETE_admin_times_서비스가_ReservationTimeInUseException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESERVATION_TIME_IN_USE))
                .given(reservationTimeService).deleteReservationTime(3L);

        mockMvc.perform(delete("/admin/times/3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_TIME_IN_USE"));
    }
}