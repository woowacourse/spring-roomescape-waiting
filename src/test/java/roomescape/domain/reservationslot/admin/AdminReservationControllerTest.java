package roomescape.domain.reservationslot.admin;


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
import roomescape.domain.reservation.ReservationService;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.admin.AdminReservationController;
import roomescape.domain.reservation.admin.dto.ReservationResponse;
import roomescape.domain.reservation.admin.dto.ReservationResponse.ReservationTimePayload;
import roomescape.domain.reservation.admin.dto.ReservationResponse.ThemePayload;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.auth.AdminRequestValidator;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AdminRequestValidator validator;

    @Test
    @DisplayName("관리자가 대기 목록 조회 시 요청과 응답을 확인한다.")
    void getWaitingReservations() throws Exception {
        // given
        ReservationResponse response = new ReservationResponse(
            1L,
            LocalDate.of(2026, 5, 10),
            ReservationTimePayload.from(ReservationTime.of(2L, LocalTime.of(10, 10))),
            ThemePayload.from(Theme.of(3L, "공포", "으악 무서워!", "theme-url")),
            "보예",
            1L,
            ReservationStatus.WAITING
        );
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);
        given(reservationService.getWaitingReservations())
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/admin/waitings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].waitingNumber").value(1))
            .andExpect(jsonPath("$[0].reservationStatus").value("WAITING"));

        verify(reservationService).getWaitingReservations();
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 대기 목록 조회 시 401을 반환한다.")
    void getWaitingReservationsWhenUnauthorized() throws Exception {
        // given
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(get("/admin/waitings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-ADMIN-TOKEN", "wrong-token"))
            .andExpect(status().isUnauthorized());

        verify(reservationService, never()).getWaitingReservations();
    }

    @Test
    @DisplayName("관리자가 대기 예약 삭제 시 요청과 응답을 확인한다.")
    void cancelWaitingReservation() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(false);

        // when & then
        mockMvc.perform(delete("/admin/waitings/{id}", id)
                .header("X-ADMIN-TOKEN", "token"))
            .andExpect(status().isNoContent());

        verify(reservationService).cancelReservationByAdmin(id);
    }

    @Test
    @DisplayName("관리자 인증에 실패하면 대기 예약 삭제 시 401을 반환한다.")
    void cancelReservationWhenUnauthorized() throws Exception {
        // given
        Long id = 1L;
        when(validator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/admin/waitings/{id}", id)
                .header("X-ADMIN-TOKEN", "wrong-token"))
            .andExpect(status().isUnauthorized());

        verify(reservationService, never()).cancelReservationByAdmin(id);
    }
}
