package roomescape.reservationWaiting.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.service.ReservationWaitingService;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@WebMvcTest(ReservationWaitingController.class)
class ReservationWaitingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReservationWaitingService reservationWaitingService;

    @DisplayName("ReservationWaitingRequest를 받아 예약 대기를 생성하고 201을 반환한다.")
    @Test
    void createReservationWaiting_success() throws Exception {
        //given
        when(reservationWaitingService.makeReservationWaiting(any()))
                .thenReturn(new ReservationWaiting(
                        1L,
                        "brown",
                        LocalDate.of(2026, 6, 1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "테마", "설명", "url"))
                );

        String body = """
                {
                    "name": "brown",
                    "date": "2026-06-01",
                    "timeId": "1",
                    "themeId": "1"
                }
                """;

        //when & then
        mockMvc.perform(
                post("/reservation-waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andExpect(status().isCreated());
    }

    @DisplayName("예약 대기 생성 시에, 필드가 하나라도 생략되면 400을 반환한다.")
    @Test
    void createReservationWaiting_no_field() throws Exception {
        //given
        when(reservationWaitingService.makeReservationWaiting(any()))
                .thenReturn(new ReservationWaiting(
                        1L,
                        "brown",
                        LocalDate.of(2026, 6, 1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "테마", "설명", "url"))
                );

        String noName = """
                {
                    "date": "2026-06-01",
                    "timeId": "1",
                    "themeId": "1"
                }
                """;

        String noDate = """
                {
                    "name": "brown",
                    "timeId": "1",
                    "themeId": "1"
                }
                """;

        String noTimeId = """
                {
                    "name": "brown",
                    "date": "2026-06-01",
                    "themeId": "1"
                }
                """;

        String noThemeId = """
                {
                    "name": "brown",
                    "date": "2026-06-01",
                    "timeId": "1"
                }
                """;

        //when & then
        mockMvc.perform(
                post("/reservation-waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noName)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservation-waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noDate)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservation-waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noTimeId)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservation-waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noThemeId)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("예약 대기 생성 시에, 필드가 하나라도 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void createReservationWaiting_invalid_field() throws Exception {
        //given
        when(reservationWaitingService.makeReservationWaiting(any()))
                .thenReturn(new ReservationWaiting(
                        1L,
                        "brown",
                        LocalDate.of(2026, 6, 1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "테마", "설명", "url"))
                );


        String invalidDate = """
                {
                    "name": "brown",
                    "date": "invalid",
                    "timeId": "1",
                    "themeId": "1"
                }
                """;

        String invalidTimeId = """
                {
                    "name": "brown",
                    "date": "2026-06-01",
                    "timeId": "invalid",
                    "themeId": "1"
                }
                """;

        String invalidThemeId = """
                {
                    "name": "brown",
                    "date": "2026-06-01",
                    "timeId": "1",
                    "themeId": "invalid"
                }
                """;

        //when & then
        mockMvc.perform(
                post("/reservation-waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDate)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservation-waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTimeId)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservation-waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidThemeId)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("id를 받아 예약 대기를 삭제하고 204를 반환한다.")
    @Test
    void deleteTime_success() throws Exception {
        mockMvc.perform(
                delete("/reservation-waitings/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "brown")
        ).andExpect(status().isNoContent());
    }

    @DisplayName("예약 대기 삭제 시, id 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void deleteTime_invalid_id() throws Exception {
        mockMvc.perform(
                delete("/reservation-waitings/{id}", "invalid")
                        .header(HttpHeaders.AUTHORIZATION, "brown")
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("예약 대기 삭제 시, Authorization 헤더가 없으면 401을 반환한다.")
    @Test
    void deleteTime_unAuthenticated() throws Exception {
        mockMvc.perform(
                delete("/reservation-waitings/{id}", "invalid")
        ).andExpect(status().isUnauthorized());
    }
}
