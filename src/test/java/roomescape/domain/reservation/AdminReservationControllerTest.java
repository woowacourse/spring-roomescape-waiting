package roomescape.domain.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.admin.AdminRequestValidator;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.RoomescapeException;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    private static final String ADMIN_HEADER = "X-ADMIN-TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AdminRequestValidator adminRequestValidator;

    @Test
    void 관리자_예약_목록_조회_요청을_처리하고_200을_반환한다() throws Exception {
        when(reservationService.getAllReservations())
            .thenReturn(List.of(new ReservationResponse(
                1L,
                "고래",
                LocalDate.of(2026, 5, 10),
                new ReservationResponse.ReservationTimePayload(2L, LocalTime.of(10, 0)),
                new ReservationResponse.ThemePayload(3L, "공포", "테마 내용", "/themes/scary")
            )));

        mockMvc.perform(get("/admin/reservations")
                .header(ADMIN_HEADER, "token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("고래"));

        verify(reservationService).getAllReservations();
    }

    @Test
    void 관리자_예약_삭제_요청을_처리하고_204를_반환한다() throws Exception {
        mockMvc.perform(delete("/admin/reservations/1")
                .header(ADMIN_HEADER, "token"))
            .andExpect(status().isNoContent());

        verify(reservationService).deleteReservation(1L);
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_404를_반환한다() throws Exception {
        doThrow(new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_FOUND))
            .when(reservationService)
            .deleteReservation(999L);

        mockMvc.perform(delete("/admin/reservations/999")
                .header(ADMIN_HEADER, "token"))
            .andExpect(status().isNotFound());
    }

    @Test
    void 관리자_토큰이_없으면_401을_반환한다() throws Exception {
        when(adminRequestValidator.isUnauthorized(any(HttpServletRequest.class))).thenReturn(true);

        mockMvc.perform(get("/admin/reservations"))
            .andExpect(status().isUnauthorized());
    }
}
