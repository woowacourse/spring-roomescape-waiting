package roomescape.controller;

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
import roomescape.auth.LoginMemberArgumentResolver;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.domain.MemberRole;
import roomescape.service.MemberService;
import roomescape.service.ReservationTimeService;
import roomescape.service.param.CreateReservationTimeParam;
import roomescape.service.result.MemberResult;
import roomescape.service.result.ReservationTimeResult;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationTimeController.class)
class ReservationTimeControllerTest {

    private static final String TEST_EMAIL = "test@email.com";
    private static final String TEST_NAME = "test";
    private static final String VALID_TOKEN = "valid.token.here";
    private static final LocalTime TEST_TIME = LocalTime.of(12, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CookieProvider cookieProvider;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;


    @Test
    @DisplayName("예약 시간을 생성할 수 있다.")
    void createReservationTime() throws Exception {
        // given
        String timeJson = String.format("""
                {
                    "startAt": "%s"
                }
                """, TEST_TIME);

        ReservationTimeResult timeResult = new ReservationTimeResult(1L, TEST_TIME);
        MemberResult memberResult = new MemberResult(1L, TEST_NAME, MemberRole.ADMIN, TEST_EMAIL);

        when(cookieProvider.extractTokenFromCookie(any())).thenReturn(VALID_TOKEN);
        when(jwtTokenProvider.extractIdFromToken(VALID_TOKEN)).thenReturn(1L);
        when(memberService.findById(1L)).thenReturn(memberResult);
        when(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(LoginMemberInfo.of(1L));
        when(reservationTimeService.create(any(CreateReservationTimeParam.class))).thenReturn(timeResult);

        // when & then
        mockMvc.perform(post("/times")
                .contentType(MediaType.APPLICATION_JSON)
                .content(timeJson)
                .cookie(TestFixture.createAuthCookie(VALID_TOKEN))
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(timeResult.id()));
    }

    @Test
    @DisplayName("예약 시간 목록을 조회할 수 있다.")
    void getReservationTimes() throws Exception {
        // given
        ReservationTimeResult timeResult = new ReservationTimeResult(1L, TEST_TIME);
        when(reservationTimeService.findAll()).thenReturn(List.of(timeResult));

        // when & then
        mockMvc.perform(get("/times"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(timeResult.id()));
    }

    @Test
    @DisplayName("예약 시간을 삭제할 수 있다.")
    void deleteReservationTime() throws Exception {
        // given
        MemberResult memberResult = new MemberResult(1L, TEST_NAME, MemberRole.ADMIN, TEST_EMAIL);
        when(cookieProvider.extractTokenFromCookie(any())).thenReturn(VALID_TOKEN);
        when(jwtTokenProvider.extractIdFromToken(VALID_TOKEN)).thenReturn(1L);
        when(memberService.findById(1L)).thenReturn(memberResult);
        when(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(LoginMemberInfo.of(1L));

        // when & then
        mockMvc.perform(delete("/times/1")
                .cookie(TestFixture.createAuthCookie(VALID_TOKEN))
        )
                .andDo(print())
                .andExpect(status().isNoContent());
    }
} 