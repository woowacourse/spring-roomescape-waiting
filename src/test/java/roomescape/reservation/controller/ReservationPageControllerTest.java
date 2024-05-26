package roomescape.reservation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import roomescape.util.ControllerTest;

@DisplayName("예약 페이지 테스트")
class ReservationPageControllerTest extends ControllerTest {

    @DisplayName("기본 페이지 조회 시, 200을 반환한다.")
    @Test
    void adminMainPage() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("예약 페이지 조회 시, 200을 반환한다.")
    @Test
    void getAdminReservationPage() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/reservation")
                        .cookie(new Cookie("token", memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("나의 예약 페이지 조회 시, 200을 반환한다.")
    @Test
    void getAdminReservationTimePage() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/reservation-mine")
                        .cookie(new Cookie("token", memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}
