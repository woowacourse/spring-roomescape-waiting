package roomescape.controller;

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
import roomescape.dto.request.CreateReservationTimeRequest;
import roomescape.exception.ReservationTimeInUseException;
import roomescape.infrastructure.AdminAuthorizationInterceptor;
import roomescape.infrastructure.LoginCheckInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.ReservationTimeService;

@WebMvcTest(controllers = AdminReservationTimeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, LoginCheckInterceptor.class,
                        AdminAuthorizationInterceptor.class, LoginUserArgumentResolver.class}))
class AdminReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    void POST_admin_times_생성된_id를_Location_헤더에_담아_201을_반환한다() throws Exception {
        given(reservationTimeService.createReservationTime(any(CreateReservationTimeRequest.class)))
                .willReturn(new ReservationTime(5L, LocalTime.of(10, 0)));

        Map<String, Object> body = Map.of("startAt", "10:00");

        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/times/5"));
    }

    @Test
    void DELETE_admin_times_id_204를_반환하고_서비스에_위임한다() throws Exception {
        mockMvc.perform(delete("/admin/times/3"))
                .andExpect(status().isNoContent());

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
                .andExpect(jsonPath("$.message")
                        .value("'startAt' 값 'abc'은(는) HH:mm 형식이어야 합니다."));
    }

    @Test
    void POST_admin_times_본문의_startAt이_누락되면_400과_메시지를_반환한다() throws Exception {
        String body = "{}";

        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("startAt은(는) 필수 입력값입니다."));
    }

    @Test
    void DELETE_admin_times_서비스가_ResourceNotFoundException을_던지면_404과_메시지를_반환한다() throws Exception {
        willThrow(new roomescape.exception.ResourceNotFoundException("예약 시간", 9999L))
                .given(reservationTimeService).deleteReservationTime(9999L);

        mockMvc.perform(delete("/admin/times/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약 시간을(를) 찾을 수 없습니다. id=9999"));
    }

    @Test
    void DELETE_admin_times_서비스가_ReservationTimeInUseException을_던지면_409과_메시지를_반환한다() throws Exception {
        willThrow(new ReservationTimeInUseException())
                .given(reservationTimeService).deleteReservationTime(3L);

        mockMvc.perform(delete("/admin/times/3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("예약이 존재하는 시간은 삭제할 수 없습니다."));
    }
}
