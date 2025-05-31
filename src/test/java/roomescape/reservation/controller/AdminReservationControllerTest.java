package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import roomescape.auth.infrastructure.jwt.JwtTokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Member adminMember;
    private Theme theme;
    private ReservationTime time;
    private String adminToken;

    @BeforeEach
    void setUp() {
        adminMember = memberRepository.save(Member.withRole("관리자", "admin@example.com", "password", MemberRole.ADMIN));
        theme = themeRepository.save(Theme.of("테마명", "테마 설명", "thumbnail.jpg"));
        time = reservationTimeRepository.save(ReservationTime.from(LocalTime.of(13, 0)));
        adminToken = jwtTokenProvider.createToken(Jwts.claims().subject(adminMember.getId().toString()).build());
    }

    @Test
    void 관리자_예약_생성_성공() throws Exception {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime currentDateTime = LocalDateTime.now();
        String content = String.format("{" +
                        "\"date\": \"%s\"," +
                        "\"timeId\": %d," +
                        "\"themeId\": %d," +
                        "\"memberId\": %d" +
                        "}",
                date.toString(),
                time.getId(),
                theme.getId(),
                adminMember.getId());

        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", adminToken))
                        .content(content))
                .andExpect(status().isCreated());
    }

    @Test
    void 관리자_예약_취소_성공() throws Exception {
        // given
        LocalDateTime currentDateTime = LocalDateTime.now();
        Reservation reservation = Reservation.of(
                LocalDate.now().plusDays(1),
                time,
                theme,
                adminMember,
                currentDateTime
        );
        Reservation savedReservation = reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(delete(String.format("/admin/reservations/%d", savedReservation.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", adminToken)))
                .andExpect(status().isNoContent());

        assertThat(reservationRepository.findById(savedReservation.getId())).isEmpty();
    }

    @Test
    void 관리자_예약_시간_검증_실패() throws Exception {
        // given
        LocalDate date = LocalDate.now().minusDays(1);
        String content = String.format("{" +
                        "\"date\": \"%s\"," +
                        "\"timeId\": %d," +
                        "\"themeId\": %d," +
                        "\"memberId\": %d" +
                        "}",
                date.toString(),
                time.getId(),
                theme.getId(),
                adminMember.getId());

        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", adminToken))
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 관리자_권한_없는_유저_실패() throws Exception {
        // given
        Member userMember = memberRepository.save(Member.withDefaultRole("유저", "user@example.com", "password"));
        String userToken = jwtTokenProvider.createToken(Jwts.claims().subject(userMember.getId().toString()).build());
        LocalDate date = LocalDate.now().plusDays(1);
        String content = String.format("{" +
                        "\"date\": \"%s\"," +
                        "\"timeId\": %d," +
                        "\"themeId\": %d," +
                        "\"memberId\": %d" +
                        "}",
                date.toString(),
                time.getId(),
                theme.getId(),
                userMember.getId());

        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", userToken))
                        .content(content))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_예약_시간_중복_검증() throws Exception {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime currentDateTime = LocalDateTime.now();
        Reservation reservation = Reservation.of(
                date,
                time,
                theme,
                adminMember,
                currentDateTime
        );
        reservationRepository.save(reservation);

        String content = String.format("{" +
                        "\"date\": \"%s\"," +
                        "\"timeId\": %d," +
                        "\"themeId\": %d," +
                        "\"memberId\": %d" +
                        "}",
                date.toString(),
                time.getId(),
                theme.getId(),
                adminMember.getId());

        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", adminToken))
                        .content(content))
                .andExpect(status().isBadRequest());
    }
}
