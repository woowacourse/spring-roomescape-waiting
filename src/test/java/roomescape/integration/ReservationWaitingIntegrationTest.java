package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.controller.request.TokenLoginCreateRequest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.role.Role;
import roomescape.member.service.AuthService;
import roomescape.member.service.MemberRepository;
import roomescape.reservation.controller.request.ReservationRequest;
import roomescape.reservation.controller.request.WaitingCreateRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.service.ReservationRepository;
import roomescape.reservation.service.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationWaitingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private EntityManager em;

    private Theme theme;
    private Member member1;
    private Member member2;
    private ReservationTime reservationTime;
    private LocalDate futureDate;
    private Cookie tokenCookie;
    private Cookie tokenCookie2;

    @BeforeEach
    void setUp() {
        theme = themeRepository.save(new Theme("테마1", "설명1", "썸네일1"));
        member1 = memberRepository.save(
                new Member(new Name("예약자"), new Email("reserver@test.com"), new Password("1234"), Role.ADMIN));
        member2 = memberRepository.save(
                new Member(new Name("대기자"), new Email("waiter@test.com"), new Password("1234"), Role.MEMBER));
        reservationTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        futureDate = LocalDate.now().plusDays(1);
        String token = authService.loginByToken(new TokenLoginCreateRequest("reserver@test.com", "1234"))
                .tokenResponse();
        String token2 = authService.loginByToken(new TokenLoginCreateRequest("waiter@test.com", "1234"))
                .tokenResponse();
        tokenCookie = new Cookie("token", token);
        tokenCookie2 = new Cookie("token", token2);
    }

    @Test
    void 예약_생성_대기_등록_예약취소시_대기자가_예약자가_된다() throws Exception {
        // 1. 첫 번째 회원이 예약 생성
        ReservationRequest reservationRequest = new ReservationRequest(
                futureDate,
                reservationTime.getId(),
                theme.getId()
        );

        mockMvc.perform(post("/reservations")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated());

        Reservation reservation = reservationRepository.findByThemeIdAndReservationTimeIdAndReservationDate_reservationDate(
                theme.getId(), reservationTime.getId(), futureDate).get();
        assertThat(reservation.getMember().getId()).isEqualTo(member1.getId());

        // 2. 두 번째 회원이 대기 등록
        WaitingCreateRequest waitingRequest = new WaitingCreateRequest(
                futureDate,
                theme.getId(),
                reservationTime.getId()
        );

        mockMvc.perform(post("/waitings")
                        .cookie(tokenCookie2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(waitingRequest)))
                .andExpect(status().isCreated());

        Waiting waiting = waitingRepository.findFirstOrderById(theme.getId(), reservationTime.getId(), futureDate)
                .get();
        assertThat(waiting.getMember().getId()).isEqualTo(member2.getId());

        // 3. 첫 번째 회원이 예약 취소하면 두 번째 회원이 예약자가 됨
        mockMvc.perform(delete("/admin/reservations/" + reservation.getId())
                        .cookie(tokenCookie))
                .andExpect(status().isNoContent());

        em.flush();
        em.clear();

        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).get();
        assertThat(updatedReservation.getMember().getId()).isEqualTo(member2.getId());

        // 대기는 삭제되어야 함
        assertThat(waitingRepository.findFirstOrderById(theme.getId(), reservationTime.getId(), futureDate)).isEmpty();
    }

    @Test
    void 예약_취소시_대기자가_없으면_예약이_삭제된다() throws Exception {
        // 1. 예약 생성
        ReservationRequest reservationRequest = new ReservationRequest(
                futureDate,
                reservationTime.getId(),
                theme.getId()
        );

        mockMvc.perform(post("/reservations")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated());

        Reservation reservation = reservationRepository.findByThemeIdAndReservationTimeIdAndReservationDate_reservationDate(
                theme.getId(), reservationTime.getId(), futureDate).get();

        // 2. 예약 취소
        mockMvc.perform(delete("/admin/reservations/" + reservation.getId())
                        .cookie(tokenCookie))
                .andExpect(status().isNoContent());

        // 3. 예약이 삭제되었는지 확인
        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    void 이미_예약된_시간에는_새로운_예약을_할_수_없다() throws Exception {
        // 1. 첫 번째 예약 생성
        ReservationRequest firstRequest = new ReservationRequest(
                futureDate,
                reservationTime.getId(),
                theme.getId()
        );

        mockMvc.perform(post("/reservations")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // 2. 같은 시간에 두 번째 예약 시도
        ReservationRequest secondRequest = new ReservationRequest(
                futureDate,
                reservationTime.getId(),
                theme.getId()
        );

        mockMvc.perform(post("/reservations")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약자는_자신의_예약에_대기할_수_없다() throws Exception {
        // 1. 예약 생성
        ReservationRequest reservationRequest = new ReservationRequest(
                futureDate,
                reservationTime.getId(),
                theme.getId()
        );

        mockMvc.perform(post("/reservations")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated());

        // 2. 같은 예약에 대기 시도
        WaitingCreateRequest waitingRequest = new WaitingCreateRequest(
                futureDate,
                theme.getId(),
                reservationTime.getId()
        );

        mockMvc.perform(post("/waitings")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(waitingRequest)))
                .andExpect(status().isBadRequest());
    }
} 
