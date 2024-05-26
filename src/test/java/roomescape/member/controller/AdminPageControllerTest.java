package roomescape.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import roomescape.util.ControllerTest;

@DisplayName("관리자 페이지 테스트")
class AdminPageControllerTest extends ControllerTest {

    @DisplayName("관리자 메인 페이지 조회 시, 200을 반환한다.")
    @Test
    void adminMainPage() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/admin")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("관리자 예약 페이지 조회 시, 200을 반환한다.")
    @Test
    void getAdminReservationPage() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/admin/reservation")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("관리자 시간 관리 페이지 조회 시, 200을 반환한다.")
    @Test
    void getAdminReservationTimePage() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/admin/time")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("관리자 테마 페이지 조회 시, 200을 반환한다.")
    @Test
    void getAdminThemePage() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/admin/theme")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("관리자 예약 대기 페이지 조회 시, 200을 반환한다.")
    @Test
    void getAdminWaitingPage() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/admin/waiting")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}
