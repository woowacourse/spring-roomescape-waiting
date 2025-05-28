package roomescape.reservation.controller.admin;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.login.authorization.JwtTokenProvider;
import roomescape.member.login.authorization.LoginAuthorizationInterceptor;
import roomescape.member.login.authorization.TokenAuthorizationHandler;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@WebMvcTest(AdminWaitingApiController.class)
public class AdminWaitingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private TokenAuthorizationHandler tokenAuthorizationHandler;
    @MockitoBean
    private LoginAuthorizationInterceptor loginAuthorizationInterceptor;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String URI = "/admin/waitings";

    @DisplayName("어드민 예약 대기 내역을 모두 조회한다")
    @Test
    void findAllWaitings() throws Exception {
        Reservation reservation = new Reservation(
                new Member(1L, "test", "test@test.com", "password", Role.USER),
                LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "theme", "description", "thumbnail"),
                Status.WAITING
        );

        List<WaitingResponse> expectedResponses = List.of(
                new WaitingResponse(
                        reservation.getId(),
                        reservation.getMember().getName(),
                        reservation.getTheme().getName(),
                        reservation.getDate(),
                        reservation.getTime().getStartAt()
                )
        );
        when(reservationService.findAllWaiting()).thenReturn(expectedResponses);

        mockMvc.perform(get(URI).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }
}
