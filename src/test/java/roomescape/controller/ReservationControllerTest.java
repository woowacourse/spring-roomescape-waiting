package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationStatus;
import roomescape.dto.response.ReservationRankResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void 예약_생성() throws Exception {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        ReservationResponse response = new ReservationResponse(
                1L,
                "브라운",
                LocalDate.now().plusDays(1),
                new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                new ThemeResponse(1L, "우테코 공포물", "레벨2 미션의 공포", "/horror"),
                ReservationStatus.CONFIRMED,
                null,
                null
        );
        given(reservationService.save(any())).willReturn(response);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"))
                .andExpect(jsonPath("$.timeResponse.id").value(1))
                .andExpect(jsonPath("$.themeResponse.id").value(1));
    }

    @Test
    void 예약_목록_조회() throws Exception {
        ReservationResponse response = new ReservationResponse(
                1L,
                "브라운",
                LocalDate.now(),
                new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                new ThemeResponse(1L, "우테코 공포물", "레벨2 미션의 공포", "/horror"),
                ReservationStatus.CONFIRMED,
                null,
                null
        );
        given(reservationService.findAll()).willReturn(List.of(response));

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"));
    }

    @Test
    void 이름_기반_예약_조회() throws Exception {
        ReservationRankResponse response = new ReservationRankResponse(
                1L,
                "아나키",
                LocalDate.now(),
                new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                new ThemeResponse(1L, "우테코 공포물", "레벨2 미션의 공포", "/horror"),
                ReservationStatus.WAITING,
                1L,
                null,
                null
        );
        given(reservationService.find("아나키")).willReturn(List.of(response));

        mockMvc.perform(get("/reservations/my-reservation")
                        .param("name", "아나키"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].order").value(1));
    }

    @Test
    void 예약_변경() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("date", LocalDate.now().plusDays(2).toString());
        request.put("timeId", 2);

        ReservationResponse response = new ReservationResponse(
                1L,
                "브라운",
                LocalDate.now().plusDays(2),
                new ReservationTimeResponse(2L, LocalTime.of(11, 0)),
                new ThemeResponse(1L, "우테코 공포물", "레벨2 미션의 공포", "/horror"),
                ReservationStatus.CONFIRMED,
                null,
                null
        );
        given(reservationService.update(any(), any())).willReturn(response);

        mockMvc.perform(patch("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.timeResponse.id").value(2));
    }

    @Test
    void 예약_삭제() throws Exception {
        willDoNothing().given(reservationService).delete(1L);

        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isNoContent());
    }
}
