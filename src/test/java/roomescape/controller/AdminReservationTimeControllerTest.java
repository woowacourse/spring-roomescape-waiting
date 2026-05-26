package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.ReservationTimeRequest;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.ReservationTimeResult;

@WebMvcTest(AdminReservationTimeController.class)
class AdminReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("GET /admin/times - 시간 목록을 반환한다")
    void list() throws Exception {
        given(reservationTimeService.findAll())
                .willReturn(List.of(new ReservationTimeResult(1L, LocalTime.of(10, 0))));

        mockMvc.perform(get("/admin/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].startAt").value("10:00"));
    }

    @Test
    @DisplayName("POST /admin/times - 유효한 요청이면 시간을 생성한다")
    void create() throws Exception {
        given(reservationTimeService.create(any()))
                .willReturn(new ReservationTimeResult(1L, LocalTime.of(10, 0)));
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));

        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startAt").value("10:00"));
    }

    @Test
    @DisplayName("POST /admin/times - startAt이 없으면 400을 반환한다")
    void create_invalid() throws Exception {
        mockMvc.perform(post("/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/times/{id} - 시간을 삭제한다")
    void delete_() throws Exception {
        mockMvc.perform(delete("/admin/times/1"))
                .andExpect(status().isOk());
    }
}
