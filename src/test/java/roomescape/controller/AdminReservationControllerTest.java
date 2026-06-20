package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.ReservationRequest;
import roomescape.service.AdminReservationService;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationTimeResult;
import roomescape.service.dto.ThemeResult;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AdminReservationService reservationService;

    @Test
    @DisplayName("GET /admin/reservations - 예약 목록을 반환한다")
    void list() throws Exception {
        given(reservationService.findAll()).willReturn(List.of(sampleResult()));

        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"));
    }

    @Test
    @DisplayName("POST /admin/reservations - 유효한 요청이면 예약을 생성한다")
    void create() throws Exception {
        given(reservationService.create(any())).willReturn(sampleResult());
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.of(2099, 12, 31), 1L, 1L);

        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("브라운"));
    }

    @Test
    @DisplayName("POST /admin/reservations - 이름이 비어있으면 400을 반환한다")
    void create_invalid() throws Exception {
        ReservationRequest request = new ReservationRequest("", LocalDate.of(2099, 12, 31), 1L, 1L);

        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/reservations/{id} - 예약을 삭제한다")
    void delete_() throws Exception {
        mockMvc.perform(delete("/admin/reservations/1"))
                .andExpect(status().isNoContent());
    }

    private ReservationResult sampleResult() {
        return new ReservationResult(
                1L, "브라운", LocalDate.of(2099, 12, 31),
                new ReservationTimeResult(1L, LocalTime.of(10, 0)),
                new ThemeResult(1L, "무인도 탈출", "설명", "https://example.com/thumb.jpg", 0L),
                0L, null, null, null, null
        );
    }
}
