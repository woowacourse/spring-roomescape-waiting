package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.TestFixture;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.controller.request.CreateReservationRequest;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.MemberResult;
import roomescape.service.result.ReservationResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private WaitingService waitingService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CookieProvider cookieProvider;

    private MemberResult memberResult;
    private ReservationResult reservationResult;
    private Cookie authCookie;

    @BeforeEach
    void setUp() {
        memberResult = TestFixture.createMemberResult();
        reservationResult = TestFixture.createDefaultReservationResult();
        authCookie = new Cookie(TestFixture.AUTH_COOKIE_NAME, TestFixture.VALID_TOKEN);

        when(cookieProvider.extractTokenFromCookies(any())).thenReturn(TestFixture.VALID_TOKEN);
        when(jwtTokenProvider.extractIdFromToken(TestFixture.VALID_TOKEN)).thenReturn(TestFixture.TEST_MEMBER_ID);
        when(memberService.findById(TestFixture.TEST_MEMBER_ID)).thenReturn(memberResult);
        when(cookieProvider.extractTokenFromCookie(any(Cookie.class))).thenReturn(TestFixture.VALID_TOKEN);
    }

    @Test
    @DisplayName("예약을 생성할 수 있다.")
    void createReservation() throws Exception {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                TestFixture.TEST_DATE,
                TestFixture.TEST_TIME_ID, 
                TestFixture.TEST_THEME_ID
        );
        when(reservationService.create(any(CreateReservationParam.class), any(LocalDateTime.class)))
                .thenReturn(reservationResult);
                
        // when & then
        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(authCookie)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reservationResult.id()))
                .andExpect(jsonPath("$.date").value(reservationResult.date().toString()));
    }

    @Test
    @DisplayName("예약 목록을 조회할 수 있다.")
    void findAllReservations() throws Exception {
        // given
        when(reservationService.findReservationsInConditions(null, null, null, null))
                .thenReturn(List.of(reservationResult));

        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reservationResult.id()))
                .andExpect(jsonPath("$[0].date").value(reservationResult.date().toString()));
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다.")
    void deleteReservation() throws Exception {
        // given
        ReservationResult reservationResult = TestFixture.createDefaultReservationResult();
        when(reservationService.findById(TestFixture.TEST_RESERVATION_ID)).thenReturn(reservationResult);
        doNothing().when(reservationService).deleteByIdAndApproveFirstWaiting(TestFixture.TEST_RESERVATION_ID);

        // when & then
        mockMvc.perform(delete("/reservations/" + TestFixture.TEST_RESERVATION_ID)
                .cookie(authCookie))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("로그인한 사용자의 예약 목록을 조회할 수 있다.")
    void getMyReservations() throws Exception {
        // given
        when(reservationService.findReservationsByMemberId(TestFixture.TEST_MEMBER_ID))
                .thenReturn(List.of(reservationResult));

        // when & them
        mockMvc.perform(get("/reservations/member")
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reservationResult.id()));
    }
}