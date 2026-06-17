package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.time.domain.ReservationTime;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.domain.Theme;

@WebMvcTest(AdminReservationController.class)
public class AdminReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @DisplayName("전체 예약 목록을 조회한다.")
    @Test
    void 예약_목록_조회_테스트() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L, LocalDateTime.of(2030, 6, 1, 10, 0), LocalDateTime.of(2030, 6, 1, 12, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation reservation = new Reservation("라이", time, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationService.getAll()).thenReturn(List.of(reservation));

        // when & then
        mockMvc.perform(get("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("라이"));
    }

    @DisplayName("예약을 생성한다.")
    @Test
    void 예약_생성_테스트() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L,
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation saved = new Reservation("라이", time, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        Mockito.when(reservationService.create(any())).thenReturn(saved);

        String requestBody = """
                {
                    "name": "라이",
                    "themeId": 1,
                    "timeId": 1
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("라이"));
    }

    @DisplayName("빈 문자열 이름으로 예약 요청인 경우, MethodArgumentNotValidException이 발생한다.")
    @Test
    void 빈_이름_예약_생성_400_반환_테스트() throws Exception {
        String requestBody = """
                {
                    "name": "",
                    "themeId": 1,
                    "timeId": 1
                }
                """;

        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("공백으로만 이루어진 이름으로 예약 요청인 경우, MethodArgumentNotValidException이 발생한다.")
    @Test
    void 공백_이름_예약_생성_400_반환_테스트() throws Exception {
        String requestBody = """
                {
                    "name": "   ",
                    "themeId": 1,
                    "timeId": 1
                }
                """;

        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("timeId 없이 예약 요청인 경우, MethodArgumentNotValidException이 발생한다.")
    @Test
    void timeId_누락_예약_생성_400_반환_테스트() throws Exception {
        String requestBody = """
                {
                    "name": "라이",
                    "themeId": 1
                }
                """;

        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("예약을 취소한다.")
    @Test
    void 예약_취소_테스트() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/reservations/{id}", 1L))
                .andExpect(status().isNoContent());

        Mockito.verify(reservationService).cancel(1L);
    }

    @DisplayName("존재하지 않는 예약을 취소하는 경우, 404를 반환한다.")
    @Test
    void 존재하지_않는_예약_취소_404_반환_테스트() throws Exception {
        // given
        Mockito.doThrow(new ReservationNotFoundException(999L))
                .when(reservationService).cancel(999L);

        // when & then
        mockMvc.perform(delete("/admin/reservations/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
