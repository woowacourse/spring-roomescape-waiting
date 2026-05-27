package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.JwtTokenProvider;
import roomescape.auth.Role;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.ReservationCreateRequest;
import roomescape.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ReservationController.class)
public class ReservationControllerWebMvcTest {

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final Long LOGIN_MEMBER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MemberDao memberDao;

    @BeforeEach
    void setUp() {
        doNothing().when(jwtTokenProvider).validateToken(VALID_TOKEN);
        when(jwtTokenProvider.getMemberId(VALID_TOKEN)).thenReturn(LOGIN_MEMBER_ID);

        Member loginMember = new Member(
                LOGIN_MEMBER_ID, "brown@email.com", "password", "브라운", Role.USER, null);
        when(memberDao.findById(LOGIN_MEMBER_ID)).thenReturn(loginMember);
    }

    @Test
    void 예약_생성_성공() throws Exception {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation savedReservation = new Reservation(
                1L, LOGIN_MEMBER_ID, date,
                new ReservationTime(1L, LocalTime.of(10, 0)),
                1L, 1L
        );
        when(reservationService.createReservation(
                eq(LOGIN_MEMBER_ID), any(LocalDate.class), eq(1L), eq(1L), eq(1L)
        )).thenReturn(savedReservation);

        ReservationCreateRequest body = new ReservationCreateRequest(date, 1L, 1L, 1L);

        // when & then
        mockMvc.perform(post("/api/v1/reservations")
                        .cookie(new Cookie("access_token", VALID_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.memberId").value(LOGIN_MEMBER_ID))
                .andExpect(jsonPath("$.storeId").value(1));
    }
}
