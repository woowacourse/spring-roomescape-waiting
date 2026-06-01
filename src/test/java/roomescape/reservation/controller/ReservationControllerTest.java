package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReservationService reservationService;

    @DisplayName("ReservationRequest를 받아 예약을 생성하고 201을 반환한다.")
    @Test
    void createReservation_success() throws Exception {
        //given
        when(reservationService.makeReservation(any()))
                .thenReturn(new Reservation(
                        1L,
                        "brown",
                        LocalDate.of(2026, 6, 1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "테마", "설명", "url")
                ));

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
                post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andExpect(status().isCreated());
    }

    @DisplayName("예약 생성 시에, 필드가 하나라도 생략되면 400을 반환한다.")
    @Test
    void createReservation_no_field() throws Exception {
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
                post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noName)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noDate)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noTimeId)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noThemeId)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("예약 생성 시에, 필드 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void createReservation_invalid_field() throws Exception {
        String invalidName = """
                {
                    "name": "brown1",
                    "date": "2026-06-01",
                    "timeId": "1",
                    "themeId": "1"
                }
                """;

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
                post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidName)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDate)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTimeId)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidThemeId)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("이름을 받아 해당 예약 목록을 조회하고 200을 반환한다.")
    @Test
    void getAllReservationsByName_success() throws Exception {
        //given
        when(reservationService.findReservationsByName("brown"))
                .thenReturn(List.of(new ReservationWithStatusResult(
                        1L,
                        "brown",
                        LocalDate.of(2026, 6, 1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "테마", "설명", "url"),
                        "reserved",
                        0L
                )));

        //when & then
        mockMvc.perform(
                get("/reservations")
                        .param("name", "brown")
        ).andExpect(status().isOk());
    }

    @DisplayName("예약 목록 조회 시에, 이름이 비어있으면 400을 반환한다.")
    @Test
    void getAllReservationsByName_blank_name() throws Exception {
        mockMvc.perform(
                get("/reservations")
                        .param("name", " ")
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("내 예약 수정 요청을 처리하고 204를 반환한다.")
    @Test
    void updateMyReservation_success() throws Exception {
        String body = """
                {
                    "date": "2026-06-02",
                    "timeId": "1"
                }
                """;

        mockMvc.perform(
                patch("/reservations/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "brown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andExpect(status().isNoContent());
    }

    @DisplayName("내 예약 수정 시에, Authorization 헤더가 없으면 401을 반환한다.")
    @Test
    void updateMyReservation_unAuthenticated() throws Exception {
        String body = """
                {
                    "date": "2026-06-02",
                    "timeId": "1"
                }
                """;

        mockMvc.perform(
                patch("/reservations/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andExpect(status().isUnauthorized());
    }

    @DisplayName("내 예약 삭제 요청을 처리하고 204를 반환한다.")
    @Test
    void deleteMyReservation_success() throws Exception {
        mockMvc.perform(
                delete("/reservations/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "brown")
        ).andExpect(status().isNoContent());
    }

    @DisplayName("내 예약 삭제 시에, Authorization 헤더가 없으면 401을 반환한다.")
    @Test
    void deleteMyReservation_unAuthenticated() throws Exception {
        mockMvc.perform(
                delete("/reservations/{id}", 1)
        ).andExpect(status().isUnauthorized());
    }
}
