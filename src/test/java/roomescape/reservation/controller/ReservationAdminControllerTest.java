package roomescape.reservation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@WebMvcTest(ReservationAdminController.class)
class ReservationAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReservationService reservationService;

    @DisplayName("전체 예약 목록을 조회하고 200을 반환한다.")
    @Test
    void getAllReservations_success() throws Exception {
        //given
        when(reservationService.findReservations())
                .thenReturn(List.of(new Reservation(
                        1L,
                        "brown",
                        LocalDate.of(2026, 6, 1),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "테마", "설명", "url")
                )));

        //when & then
        mockMvc.perform(
                get("/admin/reservations")
        ).andExpect(status().isOk());
    }

    @DisplayName("id를 받아 예약을 삭제하고 204를 반환한다.")
    @Test
    void deleteReservation_success() throws Exception {
        mockMvc.perform(
                delete("/admin/reservations/{id}", 1)
        ).andExpect(status().isNoContent());
    }

    @DisplayName("예약 삭제 시, id 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void deleteReservation_invalid_id() throws Exception {
        mockMvc.perform(
                delete("/admin/reservations/{id}", "invalid")
        ).andExpect(status().isBadRequest());
    }
}
