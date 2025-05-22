package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.enums.Role;
import roomescape.dto.admin.AdminReservationRequest;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;
import roomescape.util.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MemberRepository memberRepository;

    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;
    private Reservation reservation;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void beforeEach() {
        reservationTime = new ReservationTime(LocalTime.of(10, 0));
        theme = new Theme("이름", "설명", "썸네일");
        member = new Member(null, "슬링키", "email", "password", Role.ADMIN);
        reservation = new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, member);

        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);
        reservationRepository.save(reservation);
    }

    @Test
    @DisplayName("관리자 예약 생성 테스트")
    void addAdminReservation() throws Exception {
        // given
        AdminReservationRequest request = new AdminReservationRequest(
            reservation.getDate(), reservationTime.getId(), theme.getId(), member.getId());
        Member savedMember = memberRepository.findById(member.getId()).orElseThrow();
        String faketoken = jwtTokenProvider.createToken(savedMember);
        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", faketoken))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("관리자 조건별 예약 검색 테스트")
    void findReservationByConditions() throws Exception {
        // given
        Member savedMember = memberRepository.findById(member.getId()).orElseThrow();
        String faketoken = jwtTokenProvider.createToken(savedMember);
        // when & then
        mockMvc.perform(get("/admin/reservations/search").cookie(new Cookie("token", faketoken))
                        .param("themeId", theme.getId().toString())
                        .param("memberId", member.getId().toString())
                        .param("dateFrom", LocalDate.now().toString())
                        .param("dateTo", LocalDate.now().plusDays(7).toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[실패] 인증 없이 관리자 예약 생성")
    void addAdminReservationWithoutToken() throws Exception {
        // given
        AdminReservationRequest request = new AdminReservationRequest(
            reservation.getDate(), reservationTime.getId(), theme.getId(), member.getId());
        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("[실패] 일반 사용자 토큰으로 관리자 예약 생성")
    void addAdminReservationWithUserToken() throws Exception {
        // given
        Member user = new Member(null, "user", "user@email.com", "password", Role.USER);
        memberRepository.save(user);
        String userToken = jwtTokenProvider.createToken(user);
        AdminReservationRequest request = new AdminReservationRequest(
            reservation.getDate(), reservationTime.getId(), theme.getId(), user.getId());
        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", userToken))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
} 
