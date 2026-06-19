package roomescape.controller.api.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.api.ReservationTimeController;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.exception.AlreadyExistsException;
import roomescape.service.ReservationTimeService;

@WebMvcTest({AdminReservationTimeController.class, ReservationTimeController.class})
public class AdminReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    void 시간_생성() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("startAt", "22:00");

        given(reservationTimeService.save(any())).willReturn(
                new ReservationTimeResponse(1L, LocalTime.of(22, 0)));

        mockMvc.perform(post("/api/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startAt").value("22:00:00"));
    }

    @Test
    void 시간_삭제() throws Exception {
        willDoNothing().given(reservationTimeService).delete(10L);

        mockMvc.perform(delete("/api/admin/times/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 예약이_존재하는_시간_삭제_불가() throws Exception {
        willThrow(new AlreadyExistsException("해당 시간에 예약이 존재하여 삭제할 수 없습니다."))
                .given(reservationTimeService).delete(1L);

        mockMvc.perform(delete("/api/admin/times/1"))
                .andExpect(status().isConflict());
    }
}
