package roomescape.domain.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.admin.AdminRequestValidator;
import roomescape.domain.reservation.dto.ReservationCreationResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservation.dto.ReservationUpdateRequest;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AdminRequestValidator adminRequestValidator;

    @Test
    void 예약_생성_요청을_처리하고_201을_반환한다() throws Exception {
        when(reservationService.createReservation(any()))
            .thenReturn(new ReservationCreationResponse(
                1L,
                "고래",
                LocalDate.of(2026, 5, 10),
                LocalTime.of(10, 0),
                new ReservationCreationResponse.ThemePayload("공포", "테마 내용", "/themes/scary")
            ));
        String requestBody = """
                {
                    "name": "고래",
                    "dateId": 1,
                    "timeId": 2,
                    "themeId": 3
                }
                """;

        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("고래"))
            .andExpect(jsonPath("$.time").value("10:00"))
            .andExpect(jsonPath("$.theme.name").value("공포"));

        verify(reservationService).createReservation(any());
    }

    @Test
    void 예약자명이_없으면_예약_생성_요청에_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "dateId": 1,
                    "timeId": 2,
                    "themeId": 3
                }
                """;

        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 이름으로_예약_조회_요청을_처리하고_200을_반환한다() throws Exception {
        when(reservationService.getReservationsByName("고래"))
            .thenReturn(List.of(new ReservationResponse(
                1L,
                "고래",
                LocalDate.of(2026, 5, 10),
                new ReservationResponse.ReservationTimePayload(2L, LocalTime.of(10, 0)),
                new ReservationResponse.ThemePayload(3L, "공포", "테마 내용", "/themes/scary")
            )));

        mockMvc.perform(get("/reservations")
                .param("name", "고래"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("고래"))
            .andExpect(jsonPath("$[0].time.id").value(2))
            .andExpect(jsonPath("$[0].theme.id").value(3));

        verify(reservationService).getReservationsByName("고래");
    }

    @Test
    void 예약_취소_요청을_처리하고_204를_반환한다() throws Exception {
        mockMvc.perform(delete("/reservations/1"))
            .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(1L);
    }

    @Test
    void 예약_변경_요청을_처리하고_200을_반환한다() throws Exception {
        when(reservationService.updateReservation(any(Long.class), any(ReservationUpdateRequest.class)))
            .thenReturn(new ReservationResponse(
                1L,
                "고래",
                LocalDate.of(2026, 5, 11),
                new ReservationResponse.ReservationTimePayload(2L, LocalTime.of(11, 0)),
                new ReservationResponse.ThemePayload(3L, "공포", "테마 내용", "/themes/scary")
            ));
        String requestBody = """
                {
                    "dateId": 10,
                    "timeId": 20
                }
                """;

        mockMvc.perform(patch("/reservations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.date").value("2026-05-11"))
            .andExpect(jsonPath("$.time.startAt").value("11:00"));

        verify(reservationService).updateReservation(any(Long.class), any(ReservationUpdateRequest.class));
    }

    @Test
    void dateId가_없으면_예약_변경_요청에_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "timeId": 20
                }
                """;

        mockMvc.perform(patch("/reservations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }
}
