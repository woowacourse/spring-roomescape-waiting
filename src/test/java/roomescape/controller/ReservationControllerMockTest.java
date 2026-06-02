package roomescape.controller;

import static org.hamcrest.Matchers.is;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationStatus;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationOrderResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
public class ReservationControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 예약과_시간_연결() throws Exception {
        ReservationRequest request = new ReservationRequest("브라운", LocalDate.of(2027, 1, 1), 1L, 1L);
        ReservationResponse response = new ReservationResponse(
                22L, "브라운", LocalDate.of(2027, 1, 1),
                new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                new ThemeResponse(1L, "테마1", "설명", "썸네일"),
                ReservationStatus.AVAILABLE
        );
        given(reservationService.save(any())).willReturn(response);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("브라운"));
    }

    @Test
    void 이름_기반_예약_조회_API() throws Exception {
        String name = "아나키";
        ReservationOrderResponse response = new ReservationOrderResponse(
                1L,
                name,
                LocalDate.of(2026, 6, 1),
                new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                new ThemeResponse(1L, "테마1", "설명", "썸네일"),
                ReservationStatus.CONFIRMED,
                1L
        );
        given(reservationService.find(name)).willReturn(List.of(response));

        mockMvc.perform(get("/reservations/my-reservation").queryParam("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(name))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void 예약_삭제_API() throws Exception {
        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isNoContent());
    }
}
