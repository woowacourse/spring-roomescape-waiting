package roomescape.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
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
import roomescape.common.GlobalExceptionHandler;
import roomescape.member.dto.MemberResponse;
import roomescape.member.login.authentication.WebMvcConfiguration;
import roomescape.member.login.authorization.JwtTokenProvider;
import roomescape.member.login.authorization.LoginAuthorizationInterceptor;
import roomescape.member.login.authorization.TokenAuthorizationHandler;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.service.ReservationService;

@WebMvcTest(MemberReservationApiController.class)
@Import({WebMvcConfiguration.class, GlobalExceptionHandler.class})
class MemberReservationApiControllerTest {

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

    private static final String URI = "/reservations-mine";

    @DisplayName("로그인한 회원의 예약 내역을 모두 조회한다")
    @Test
    void findAllByMemberId() throws Exception {
        MemberResponse memberResponse = new MemberResponse(1L, "name", "email");
        MyReservationResponse myReservationResponse = new MyReservationResponse(
                1L,
                "theme1",
                LocalDate.now().plusDays(1),
                LocalTime.now().plusHours(1),
                "예약"
        );

        when(tokenAuthorizationHandler.extractToken(any()))
                .thenReturn("test-token");
        when(memberService.findByToken("test-token"))
                .thenReturn(memberResponse);
        when(reservationService.findAllByMemberId(1L))
                .thenReturn(List.of(myReservationResponse));

        mockMvc.perform(get(URI)
                        .cookie(new Cookie("token", "test-token"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
