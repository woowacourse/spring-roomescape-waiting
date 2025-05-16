package roomescape.controller;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    private static final String VALID_TOKEN = "header.payload.signature";
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 1, 1);
    private static final Long TEST_THEME_ID = 1L;
    private static final Long TEST_TIME_ID = 1L;
    private static final Long TEST_MEMBER_ID = 1L;

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


    @BeforeEach
    void setUp() {
        when(cookieProvider.extractTokenFromCookies(any())).thenReturn(VALID_TOKEN);
        when(jwtTokenProvider.extractMemberRoleFromToken(VALID_TOKEN)).thenReturn(MemberRole.ADMIN);
    }

    @Test
    @DisplayName("관리자는 예약을 생성할 수 있다.")
    void createAdminReservation() throws Exception {
        ReservationResult expectedResult = createTestReservationResult();

        when(reservationService.create(any(CreateReservationParam.class), any(LocalDateTime.class)))
                .thenReturn(expectedResult);

        mockMvc.perform(post("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createTestReservationJson())
                .cookie(TestFixture.createAuthCookie(VALID_TOKEN))
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedResult.id()))
                .andExpect(jsonPath("$.date").value(expectedResult.date().toString()));
    }

    @Test
    @DisplayName("관리자가 아닌 사용자는 예약을 생성할 수 없다.")
    void createReservationWithNonAdminRole() throws Exception {
        when(jwtTokenProvider.extractMemberRoleFromToken(VALID_TOKEN)).thenReturn(MemberRole.USER);

        mockMvc.perform(post("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createTestReservationJson())
                .cookie(TestFixture.createAuthCookie(VALID_TOKEN))
        )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("토큰이 없는 경우 예약을 생성할 수 없다.")
    void createReservationWithoutToken() throws Exception {
        when(cookieProvider.extractTokenFromCookies(any())).thenReturn(null);

        mockMvc.perform(post("/admin/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createTestReservationJson())
        )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    private ReservationResult createTestReservationResult() {
        return new ReservationResult(
                1L,
                MemberResult.from(TestFixture.createDefaultMember()),
                TEST_DATE,
                ReservationTimeResult.from(TestFixture.createDefaultReservationTime()),
                ThemeResult.from(TestFixture.createDefaultTheme()),
                ReservationStatus.RESERVED
        );
    }

    private String createTestReservationJson() {
        return String.format("""
                {
                    "date": "%s",
                    "themeId": %d,
                    "timeId": %d,
                    "memberId": %d
                }
                """, TEST_DATE, TEST_THEME_ID, TEST_TIME_ID, TEST_MEMBER_ID);
    }
}