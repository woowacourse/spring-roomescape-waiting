package roomescape.presentation.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.reservation.ReservationService;
import roomescape.common.auth.AdminRequestValidator;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;
import roomescape.presentation.error.GlobalExceptionHandler;
import roomescape.presentation.reservation.request.ReservationUpdateRequest;
import roomescape.presentation.reservation.response.ReservationUpdateResponse;
import roomescape.presentation.reservation.response.ReservationsResponse;

@DisplayName("관리자 예약 컨트롤러")
@WebMvcTest(controllers = AdminReservationController.class)
@Import(GlobalExceptionHandler.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AdminRequestValidator validator;

    @Test
    @DisplayName("관리자는 전체 예약 목록을 조회할 수 있다")
    void getAllReservation() throws Exception {
        // given
        Reservation reservation = Reservation.of(
                1L,
                User.of(10L, "홍길동"),
                ReservationSlot.of(
                        20L,
                        LocalDate.of(2030, 1, 2),
                        ReservationTime.of(30L, LocalTime.of(13, 0)),
                        Theme.of(40L, "도심 탈출", "도심 탈출 설명", "/themes/40")
                ),
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );
        given(validator.isUnauthorized(any())).willReturn(false);
        given(reservationService.getAllReservations()).willReturn(ReservationsResponse.of(List.of(reservation)));

        // when & then
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reservations[0].username").value("홍길동"))
                .andExpect(jsonPath("$.reservations[0].slot.date").value("2030-01-02"))
                .andExpect(jsonPath("$.reservations[0].slot.startAt.startAt").value("13:00"))
                .andExpect(jsonPath("$.reservations[0].slot.theme.name").value("도심 탈출"))
                .andExpect(jsonPath("$.reservations[0].status").value("CONFIRMED"));

        verify(validator, times(1)).isUnauthorized(any());
        verify(reservationService, times(1)).getAllReservations();
    }

    @Test
    @DisplayName("권한이 없으면 전체 예약 목록을 조회할 수 없다")
    void getAllReservationWhenUnauthorized() throws Exception {
        // given
        given(validator.isUnauthorized(any())).willReturn(true);

        // when & then
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isUnauthorized());

        verify(validator, times(1)).isUnauthorized(any());
        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("관리자는 예약을 삭제할 수 있다")
    void deleteReservation() throws Exception {
        // given
        given(validator.isUnauthorized(any())).willReturn(false);

        // when & then
        mockMvc.perform(delete("/admin/reservations/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(validator, times(1)).isUnauthorized(any());
        verify(reservationService, times(1)).deleteReservationByAdmin(1L);
    }

    @Test
    @DisplayName("관리자는 예약을 수정할 수 있다")
    void updateReservation() throws Exception {
        // given
        Reservation reservation = Reservation.of(
                1L,
                User.of(10L, "홍길동"),
                ReservationSlot.of(
                        20L,
                        LocalDate.of(2030, 1, 2),
                        ReservationTime.of(30L, LocalTime.of(13, 0)),
                        Theme.of(40L, "도심 탈출", "도심 탈출 설명", "/themes/40")
                ),
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );
        given(validator.isUnauthorized(any())).willReturn(false);
        given(reservationService.updateReservationByAdmin(eq(1L), any(ReservationUpdateRequest.class)))
                .willReturn(ReservationUpdateResponse.from(reservation));

        // when & then
        mockMvc.perform(patch("/admin/reservations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slotId": 20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.date").value("2030-01-02"))
                .andExpect(jsonPath("$.startAt").value("13:00"))
                .andExpect(jsonPath("$.theme.name").value("도심 탈출"));

        verify(validator, times(1)).isUnauthorized(any());
        verify(reservationService, times(1)).updateReservationByAdmin(eq(1L), any(ReservationUpdateRequest.class));
    }
}
