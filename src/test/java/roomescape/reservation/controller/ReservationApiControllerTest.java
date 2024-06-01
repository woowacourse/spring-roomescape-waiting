package roomescape.reservation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.config.ControllerConfig;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingService;

@WebMvcTest(ReservationApiController.class)
@Import(ControllerConfig.class)
class ReservationApiControllerTest {

    @MockBean
    private ReservationService reservationService;
    @MockBean
    private WaitingService waitingService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("예약 목록 조회에 성공하면 200 응답을 받는다.")
    void findAll() throws Exception {
        mockMvc.perform(get("/reservations")
                        .cookie(new Cookie("token", "cookieValue"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("나의 예약 내역을 조회 성공하면 200을 반환한다.")
    void myReservationsTest() throws Exception {
        mockMvc.perform(get("/reservations/me")
                        .cookie(new Cookie("token", "cookieValue"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약을 성공적으로 제거하면 204 응답을 받는다.")
    void deleteReservationRequestTest() throws Exception {
        mockMvc.perform(delete("/reservations/{id}", 1L)
                        .cookie(new Cookie("token", "cookieValue")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("예약을 테마id, 멤버id, 시작일, 종료일로 조회하면 200 응답을 받는다.")
    void findAllBySearchCondTest() throws Exception {
        mockMvc.perform(get("/reservations/search")
                        .param("themeId", "1")
                        .param("memberId", "1")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
