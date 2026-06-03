package roomescape.domain.waitingreservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.admin.AdminRequestValidator;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;

@WebMvcTest(WaitingReservationController.class)
class WaitingReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingReservationService waitingReservationService;

    @MockitoBean
    private AdminRequestValidator adminRequestValidator;

    @Test
    void 예약_대기_생성_요청을_처리하고_201을_반환한다() throws Exception {
        when(waitingReservationService.createWaitingReservation(any()))
            .thenReturn(new WaitingReservationCreationResponse(
                1L,
                "고래",
                LocalDate.of(2026, 5, 10),
                LocalTime.of(10, 0),
                new WaitingReservationCreationResponse.ThemePayload("공포", "테마 내용", "/themes/scary"),
                LocalDateTime.of(2026, 5, 1, 10, 0)
            ));
        String requestBody = """
                {
                    "name": "고래",
                    "dateId": 1,
                    "timeId": 2,
                    "themeId": 3
                }
                """;

        mockMvc.perform(post("/waiting-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("고래"))
                .andExpect(jsonPath("$.time").value("10:00"))
                .andExpect(jsonPath("$.theme.name").value("공포"));

        verify(waitingReservationService).createWaitingReservation(any());
    }

    @Test
    void 예약자명이_없으면_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "dateId": 1,
                    "timeId": 1,
                    "themeId": 1
                }
                """;

        mockMvc.perform(post("/waiting-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약자명이_공백이면_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "name": "   ",
                    "dateId": 1,
                    "timeId": 1,
                    "themeId": 1
                }
                """;

        mockMvc.perform(post("/waiting-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dateId가_없으면_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "name": "고래",
                    "timeId": 1,
                    "themeId": 1
                }
                """;

        mockMvc.perform(post("/waiting-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void timeId가_없으면_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "name": "고래",
                    "dateId": 1,
                    "themeId": 1
                }
                """;

        mockMvc.perform(post("/waiting-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void themeId가_없으면_400을_반환한다() throws Exception {
        String requestBody = """
                {
                    "name": "고래",
                    "dateId": 1,
                    "timeId": 1
                }
                """;

        mockMvc.perform(post("/waiting-reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void name_파라미터가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/waiting-reservations"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_대기_취소_요청을_처리하고_204를_반환한다() throws Exception {
        mockMvc.perform(delete("/waiting-reservations/1"))
                .andExpect(status().isNoContent());

        verify(waitingReservationService).cancelWaitingReservation(1L);
    }

    @Test
    void 이름으로_예약_대기_조회_요청을_처리하고_200을_반환한다() throws Exception {
        when(waitingReservationService.getWaitingReservationsWithRankByName("고래"))
            .thenReturn(List.of(new WaitingReservationWithRankResponse(
                1L,
                "고래",
                LocalDate.of(2026, 5, 10),
                new ReservationResponse.ReservationTimePayload(2L, LocalTime.of(10, 0)),
                new ReservationResponse.ThemePayload(3L, "공포", "테마 내용", "/themes/scary"),
                1L,
                LocalDateTime.of(2026, 5, 1, 10, 0)
            )));

        mockMvc.perform(get("/waiting-reservations")
                        .param("name", "고래"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("고래"))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].time.id").value(2))
                .andExpect(jsonPath("$[0].theme.id").value(3));

        verify(waitingReservationService).getWaitingReservationsWithRankByName("고래");
    }
}
