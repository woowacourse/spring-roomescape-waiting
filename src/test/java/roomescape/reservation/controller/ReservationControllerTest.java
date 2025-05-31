package roomescape.reservation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class ReservationControllerTest {

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

    private Member member;
    private Theme theme;
    private ReservationTime time;
    private String token;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.withDefaultRole("홍길동", "hong@example.com", "password"));
        theme = themeRepository.save(Theme.of("테마명", "테마 설명", "thumbnail.jpg"));
        time = reservationTimeRepository.save(ReservationTime.from(LocalTime.of(13, 0)));
        token = jwtTokenProvider.createToken(Jwts.claims().subject(member.getId().toString()).build());
    }

    @Test
    void 예약_생성_성공() throws Exception {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime currentDateTime = LocalDateTime.now();
        String content = String.format("{\"date\": \"%s\",\"timeId\": %d,\"themeId\": %d,\"isWaiting\": false}",
                date.toString(),
                time.getId(),
                theme.getId());

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", token))
                        .content(content))
                .andExpect(status().isCreated());
    }

    @Test
    void 예약_목록_조회_성공() throws Exception {
        // given
        LocalDateTime currentDateTime = LocalDateTime.now();
        Reservation reservation = Reservation.of(
                LocalDate.now().plusDays(1),
                time,
                theme,
                member,
                currentDateTime
        );
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/reservations")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isOk());
    }

    @Test
    void 예약_취소_성공() throws Exception {
        // given
        LocalDateTime currentDateTime = LocalDateTime.now();
        Reservation reservation = Reservation.of(
                LocalDate.now().plusDays(1),
                time,
                theme,
                member,
                currentDateTime
        );
        Reservation savedReservation = reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(delete(String.format("/reservations/%d", savedReservation.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void 대기_목록_조회_성공() throws Exception {
        // given
        LocalDateTime currentDateTime = LocalDateTime.now();
        Reservation waitingReservation = Reservation.waiting(
                LocalDate.now().plusDays(1),
                time,
                theme,
                member,
                currentDateTime,
                1L
        );
        reservationRepository.save(waitingReservation);

        // when & then
        mockMvc.perform(get("/reservations")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isOk());
    }

    @Test
    void 예약_시간_검증_실패() throws Exception {
        // given
        LocalDate date = LocalDate.now().minusDays(1);
        String content = String.format("{\"date\": \"%s\",\"timeId\": %d,\"themeId\": %d,\"isWaiting\": false}",
                date.toString(),
                time.getId(),
                theme.getId());

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", token))
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 인증_실패() throws Exception {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        String content = String.format("{\"date\": \"%s\",\"timeId\": %d,\"themeId\": %d,\"isWaiting\": false}",
                date.toString(),
                time.getId(),
                theme.getId());

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isUnauthorized());
    }
}
