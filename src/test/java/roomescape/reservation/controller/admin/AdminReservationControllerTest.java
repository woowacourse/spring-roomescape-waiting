package roomescape.reservation.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import roomescape.auth.ui.AdminAuthorizationInterceptor;
import roomescape.common.security.TokenAuthorizationHandler;
import roomescape.member.application.MemberService;
import roomescape.member.application.dto.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.dto.AdminReservationRequest;
import roomescape.reservation.application.dto.AdminReservationSearchRequest;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.ui.AdminReservationController;
import roomescape.reservationTime.application.dto.TimeResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private TokenAuthorizationHandler tokenAuthorizationHandler;
    @MockitoBean
    private AdminAuthorizationInterceptor adminAuthorizationInterceptor;

    private static final String URI = "/admin/reservations";

    @DisplayName("관리자가 새로운 예약을 추가한다")
    @Test
    void add() throws Exception {
        Member member = new Member(1L, "name", "email", "password", Role.USER);
        Theme theme = new Theme(1L, "name", "des", "thu");
        ReservationTime time = new ReservationTime(1L, LocalTime.now());

        String requestBody = """
                {
                    "date": "2026-05-05",
                    "timeId": 1,
                    "themeId": 1,
                    "memberId": 1
                }
                """;

        when(reservationService.createByAdmin(any(AdminReservationRequest.class)))
                .thenReturn(new ReservationResponse(1L, MemberResponse.from(member), ThemeResponse.from(theme),
                        LocalDate.of(2026, 5, 5), TimeResponse.from(time)));

        mockMvc.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @DisplayName("관리자가 회원, 테마, 날짜 범위로 예약 내역을 조회한다")
    @Test
    void findAllByMemberAndThemeAndDate() throws Exception {
        when(reservationService.findFiltered(
                any(AdminReservationSearchRequest.class)))
                .thenReturn(List.of());

        mockMvc.perform(get(URI)
                        .param("memberId", "1")
                        .param("themeId", "1")
                        .param("dateFrom", "2024-05-05")
                        .param("dateTo", "2024-05-06")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
