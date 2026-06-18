package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.PastReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @DisplayName("예약자의 이름으로, 예약자의 예약을 전부 조회한다.")
    @Test
    void 이름으로_예약_목록_전체_조회_테스트() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L, LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Theme theme2 = new Theme("테마", "설명", "https://img.test/a.png").withId(2L);
        List<ReservationWithWaitingOrderResponse> reservationWithWaitingOrders = List.of(
                new ReservationWithWaitingOrderResponse(1L, "라이", ReservationTimeResponse.from(time),
                        ThemeResponse.from(theme), Status.RESERVED, 0, null, null, null),
                new ReservationWithWaitingOrderResponse(1L, "라이", ReservationTimeResponse.from(time),
                        ThemeResponse.from(theme2), Status.WAITING, 3, null, null, null)
        );
        when(reservationService.getAllByName("라이")).thenReturn(reservationWithWaitingOrders);

        // when & then
        mockMvc.perform(get("/reservations")
                        .param("name", "라이")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("라이"));
    }

    @DisplayName("이름과 id로 예약을 취소 시, 204를 반환한다.")
    @Test
    void 예약_취소_테스트() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations/{id}", 1L)
                        .param("name", "name"))
                .andExpect(status().isNoContent());

        Mockito.verify(reservationService).cancelForUser(1L, "name");
    }

    @DisplayName("존재하지 않는 예약을 취소하는 경우, 404를 반환한다.")
    @Test
    void 존재하지_않는_예약_취소_404_반환_테스트() throws Exception {
        // given
        Mockito.doThrow(new ReservationNotFoundException(999L))
                .when(reservationService).cancelForUser(999L, "name");

        // when & then
        mockMvc.perform(delete("/reservations/{id}", 999L)
                        .param("name", "name"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("이미 지난 예약을 취소하는 경우, 400을 반환한다.")
    @Test
    void 지난_예약_취소_400_반환_테스트() throws Exception {
        // given
        Mockito.doThrow(PastReservationException.pastCancel())
                .when(reservationService).cancelForUser(1L, "name");

        // when & then
        mockMvc.perform(delete("/reservations/{id}", 1L)
                        .param("name", "name"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("예약 시간 변경하는 경우, 200을 반환한다.")
    @Test
    void 예약_변경_테스트() throws Exception {
        // given
        ReservationTime newTime = new ReservationTime(2L, LocalDateTime.of(2030, 6, 1, 14, 0),
                LocalDateTime.of(2030, 6, 1, 16, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation updated = new Reservation("라이", newTime, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationService.update(1L, 2L)).thenReturn(updated);

        String requestBody = """
                {
                    "timeId": 2
                }
                """;

        // when & then
        mockMvc.perform(put("/reservations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("라이"));
    }

    @DisplayName("존재하지 않는 예약을 변경하는 경우, 404를 반환한다.")
    @Test
    void 존재하지_않는_예약_변경_404_반환_테스트() throws Exception {
        // given
        when(reservationService.update(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new ReservationNotFoundException(999L));

        String requestBody = """
                {
                    "timeId": 2
                }
                """;

        // when & then
        mockMvc.perform(put("/reservations/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @DisplayName("변경하려는 시간 슬롯이 과거인 경우, 400을 반환한다.")
    @Test
    void 과거_슬롯으로_예약_변경_400_반환_테스트() throws Exception {
        // given
        when(reservationService.update(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(PastReservationException.pastReservation());

        String requestBody = """
                {
                    "timeId": 1
                }
                """;

        // when & then
        mockMvc.perform(put("/reservations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("변경하려는 시간이 이미 차 있는 경우, 409를 반환한다.")
    @Test
    void 이미_찬_시간으로_예약_변경_409_반환_테스트() throws Exception {
        // given
        when(reservationService.update(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new DuplicateReservationException());

        String requestBody = """
                {
                    "timeId": 1
                }
                """;

        // when & then
        mockMvc.perform(put("/reservations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @DisplayName("timeId 없이 변경 요청하는 경우, 400을 반환한다.")
    @Test
    void timeId_누락_예약_변경_400_반환_테스트() throws Exception {
        String requestBody = """
                {
                }
                """;

        mockMvc.perform(put("/reservations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("예약 생성 후, 201을 반환한다.")
    @Test
    void 예약이_없는_경우_예약_생성_테스트() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L,
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation saved = new Reservation("라이", time, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationService.create(any())).thenReturn(saved);

        String requestBody = """
                {
                    "name": "라이",
                    "themeId": 1,
                    "timeId": 1
                }
                """;

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("라이"))
                .andExpect(jsonPath("$.status").value(Status.RESERVED.name()));
    }

    @DisplayName("예약이 존재하는 경우, 예약 대기 생성 후, 201을 반환한다.")
    @Test
    void 예약이_존재하는_경우_예약대기_생성_테스트() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L,
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation saved = new Reservation("라이", time, theme, Status.WAITING, null, null, LocalDateTime.now()).withId(1L);
        when(reservationService.create(any())).thenReturn(saved);

        String requestBody = """
                {
                    "name": "라이",
                    "themeId": 1,
                    "timeId": 1
                }
                """;

        // when&then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("라이"))
                .andExpect(jsonPath("$.status").value(Status.WAITING.name()));
    }
}
