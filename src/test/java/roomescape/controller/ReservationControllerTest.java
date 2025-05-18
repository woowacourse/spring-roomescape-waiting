package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.TestFixture;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.controller.request.CreateReservationRequest;
import roomescape.domain.MemberRole;
import roomescape.domain.ReservationStatus;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.MemberResult;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationTimeResult;
import roomescape.service.result.ThemeResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc
class ReservationControllerTest {

    private static final String TEST_EMAIL = "test@email.com";
    private static final String TEST_NAME = "test";
    private static final String VALID_TOKEN = "header.payload.signature";
    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_THEME_ID = 1L;
    private static final Long TEST_TIME_ID = 1L;
    private static final Long TEST_RESERVATION_ID = 1L;
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalTime TEST_TIME = LocalTime.of(12, 0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CookieProvider cookieProvider;
    
    private MemberResult memberResult;
    private ReservationResult reservationResult;
    private Cookie authCookie;

    @BeforeEach
    void setUp() {
        Mockito.reset(reservationService, memberService, jwtTokenProvider, cookieProvider);
        
        memberResult = new MemberResult(TEST_MEMBER_ID, TEST_NAME, MemberRole.USER, TEST_EMAIL);
        reservationResult = createReservationResult();
        authCookie = new Cookie(TestFixture.AUTH_COOKIE_NAME, VALID_TOKEN);

        when(cookieProvider.extractTokenFromCookies(any())).thenReturn(VALID_TOKEN);
        when(jwtTokenProvider.extractIdFromToken(VALID_TOKEN)).thenReturn(TEST_MEMBER_ID);
        when(memberService.findById(TEST_MEMBER_ID)).thenReturn(memberResult);
        when(cookieProvider.extractTokenFromCookie(any(Cookie.class))).thenReturn(VALID_TOKEN);
    }

    @Test
    @DisplayName("예약을 생성할 수 있다.")
    void createReservation() throws Exception {
        // given
        CreateReservationRequest request = new CreateReservationRequest(TEST_DATE, TEST_TIME_ID, TEST_THEME_ID);
        when(reservationService.create(any(CreateReservationParam.class), any(LocalDateTime.class)))
                .thenReturn(reservationResult);
                
        // when & then
        mockMvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(authCookie)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                .andDo(print())
                .andExpect(jsonPath("$[0].id").value(reservationResult.id()))
                .andExpect(jsonPath("$[0].date").value(reservationResult.date().toString()));
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다.")
    void deleteReservation() throws Exception {
        // given
        doNothing().when(reservationService).deleteById(TEST_RESERVATION_ID);

        // when & then
        mockMvc.perform(delete("/reservations/" + TEST_RESERVATION_ID)
                .cookie(authCookie))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("로그인한 사용자의 예약 목록을 조회할 수 있다.")
    void getMyReservations() throws Exception {
        // given
        when(reservationService.findMemberReservationsById(TEST_MEMBER_ID))
                .thenReturn(List.of(reservationResult));

        // when & then
        mockMvc.perform(get("/reservations/mine")
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(reservationResult.id()));
    }

    private ReservationResult createReservationResult() {
        ReservationTimeResult timeResult = new ReservationTimeResult(TEST_TIME_ID, TEST_TIME);
        ThemeResult themeResult = new ThemeResult(TEST_THEME_ID, "테마명", "테마 설명", "thumbnail.jpg");
        return new ReservationResult(
                TEST_RESERVATION_ID,
                memberResult,
                TEST_DATE,
                timeResult,
                themeResult,
                ReservationStatus.RESERVED
        );
    }
}