package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roomescape.member.domain.Member;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.member.security.crypto.JwtTokenProvider;
import roomescape.member.security.service.MemberAuthService;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationBuilder;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.service.ReservationDetailService;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@WebMvcTest(ReservationController.class)
@TestPropertySource(properties = {"security.jwt.token.secret-key=test_secret_key",
        "security.jwt.token.expire-length=3600000"})
class ReservationControllerTest {
    private final Member member = new Member("tester", "test@email.com", "pass");
    private final Reservation reservation = new ReservationBuilder()
            .member(member)
            .date(LocalDate.MAX)
            .time(new Time(1L, LocalTime.of(12, 0)))
            .theme(new Theme(1L, "도비", "도비 방탈출", "이미지~"))
            .build();

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;
    @MockBean
    private ReservationDetailService detailService;
    @MockBean
    private MemberAuthService memberAuthService;
    @MockBean
    private MemberService memberService;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, validityInMilliseconds);
    }

    @Test
    @DisplayName("예약 정보를 정상적으로 저장하는지 확인한다.")
    void createReservation() throws Exception {
        Mockito.when(detailService.findReservationDetailId(any(ReservationCreateRequest.class)))
                .thenReturn(reservation.getDetailId());
        Mockito.when(reservationService.addReservation(any(ReservationRequest.class)))
                .thenReturn(ReservationResponse.from(reservation));
        Mockito.when(memberAuthService.isLoginMember(any()))
                .thenReturn(true);
        Mockito.when(memberAuthService.extractPayload(any()))
                .thenReturn(new MemberProfileInfo(1L, "valid", "testUser@email.com"));

        Member member = new Member(1L, "valid", "testUser@email.com", "pass");
        String token = jwtTokenProvider.createToken(member, new Date());
        String content = new ObjectMapper().registerModule(new JavaTimeModule())
                .writeValueAsString(new ReservationCreateRequest(1L, 1L, 1L, reservation.getDate()));

        mockMvc.perform(post("/reservations")
                        .cookie(new Cookie("token", token))
                        .content(content)
                        .contentType("application/Json")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("예약 정보를 정상적으로 불러오는지 확인한다.")
    void findAllReservations() throws Exception {
        Mockito.when(reservationService.findReservations())
                .thenReturn(List.of(ReservationResponse.from(reservation)));

        mockMvc.perform(get("/reservations"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약 가능한 시간을 정상적으로 불러오는지 확인한다.")
    void findAvailableTimeList() throws Exception {
        Mockito.when(reservationService.findTimeAvailability(1, LocalDate.now()))
                .thenReturn(
                        List.of(ReservationTimeAvailabilityResponse.fromTime(reservation.getTime(), true)));

        mockMvc.perform(get("/reservations/times/1?date=" + LocalDate.now()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약 정보를 정상적으로 지우는지 확인한다.")
    void deleteReservation() throws Exception {
        mockMvc.perform(delete("/reservations/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
