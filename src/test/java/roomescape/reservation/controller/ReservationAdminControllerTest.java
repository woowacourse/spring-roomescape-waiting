package roomescape.reservation.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationQueryService;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@WebMvcTest(ReservationAdminController.class)
@Import(WebMvcConfig.class)
class ReservationAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ReservationQueryService reservationQueryService;

    @MockitoBean
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("모든 예약을 성공적으로 조회한다.")
    void getAllReservations_Success() throws Exception {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), time, theme), java.time.LocalDateTime.now(), true);

        given(reservationQueryService.findAll()).willReturn(List.of(ReservationResult.from(reservation)));

        // when & then
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"));
    }

    @Test
    @DisplayName("예약을 성공적으로 삭제한다.")
    void deleteReservation_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/reservations/1"))
                .andExpect(status().isNoContent());
    }
}
