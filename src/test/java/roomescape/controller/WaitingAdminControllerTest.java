package roomescape.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.TestFixture;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.domain.MemberRole;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixture.VALID_TOKEN;

@WebMvcTest(WaitingAdminController.class)
class WaitingAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private CookieProvider cookieProvider;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        when(cookieProvider.extractTokenFromCookies(any())).thenReturn(VALID_TOKEN);
        when(jwtTokenProvider.extractMemberRoleFromToken(VALID_TOKEN)).thenReturn(MemberRole.ADMIN);
    }

    @Test
    @DisplayName("관리자는 예약 대기를 승인할 수 있다.")
    void approveWaiting() throws Exception {
        mockMvc.perform(post("/admin/waitings/" + TestFixture.TEST_WAITING_ID + "/approve"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자는 예약 대기를 거절할 수 있다.")
    void rejectWaiting() throws Exception {
        mockMvc.perform(post("/admin/waitings/" + TestFixture.TEST_WAITING_ID + "/reject"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}