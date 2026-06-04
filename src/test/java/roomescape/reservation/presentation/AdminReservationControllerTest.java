package roomescape.reservation.presentation;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpServletRequest;
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
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.presentation.AdminReservationController;
import roomescape.reservation.presentation.response.ReservationResponse;
import roomescape.reservation.presentation.response.ReservationResponse.ReservationTimePayload;
import roomescape.reservation.presentation.response.ReservationResponse.ThemePayload;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.common.auth.AdminRequestValidator;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AdminRequestValidator validator;

    @Test
    @DisplayName("관리자가 전체 예약 조회 시 요청과 응답을 확인한다.")
    void getAllReservation() throws Exception {
        // given
        ReservationResponse response = new ReservationResponse(
            1L,
            LocalDate.of(2026, 5, 10),
            ReservationTimePayload.from(ReservationTime.of(2L, LocalTime.of(10, 10))),
            ThemePayload.from(Theme.of(3L, "공포", "으악 무서워!", "theme-url")),
            "보예",
            0,
            ReservationStatus.CONFIRMED
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        given(reservationService.getAllReservations())
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].date").value("2026-05-10"))
            .andExpect(jsonPath("$[0].time.id").value(2))
            .andExpect(jsonPath("$[0].time.startAt").value("10:10"))
            .andExpect(jsonPath("$[0].theme.id").value(3))
            .andExpect(jsonPath("$[0].theme.name").value("공포"))
            .andExpect(jsonPath("$[0].theme.content").value("으악 무서워!"))
            .andExpect(jsonPath("$[0].theme.url").value("theme-url"))
            .andExpect(jsonPath("$[0].userName").value("보예"))
            .andExpect(jsonPath("$[0].waitingNumber").value(0))
            .andExpect(jsonPath("$[0].reservationStatus").value("CONFIRMED"))
        ;

        verify(reservationService).getAllReservations();
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 전체 예약 조회 시 401을 반환한다.")
    void getAllReservationWhenUnauthorized() throws Exception {
        // given
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(get("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "wrong-token"))
            .andExpect(status().isUnauthorized());

        verify(reservationService, never()).getAllReservations();
    }

    @Test
    @DisplayName("관리자가 예약 삭제 시 요청과 응답을 확인한다.")
    void cancelReservation() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);

        // when & then
        mockMvc.perform(delete("/admin/reservations/{id}", id)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isNoContent());

        verify(reservationService).cancelReservationByAdmin(id);
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 예약 삭제 시 401을 반환한다.")
    void cancelReservationWhenUnauthorized() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/admin/reservations/{id}", id)
                .header("X-ADMIN-TOKEN", "wrong-token"))
            .andExpect(status().isUnauthorized());

        verify(reservationService, never()).cancelReservationByAdmin(id);
    }
}
