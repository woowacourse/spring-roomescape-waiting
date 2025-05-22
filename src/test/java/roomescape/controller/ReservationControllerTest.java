package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.enums.Role;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;
import roomescape.repository.waiting.WaitingRepsitory;
import roomescape.util.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    ReservationTime reservationTime;
    Theme theme;
    Member member;
    Reservation reservation;
    Member waiter;
    Waiting waiting;
    @Autowired
    private WaitingRepsitory waitingRepsitory;

    @BeforeEach
    void beforeEach() {
        reservationTime = new ReservationTime(LocalTime.of(10, 0));
        theme = new Theme("이름", "설명", "썸네일");
        member = new Member(null, "슬링키", "email", "password", Role.USER);
        reservation = new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, member);
        waiter = new Member(null, "키링슬", "email", "password", Role.USER);
        waiting = new Waiting(null, LocalDate.now().plusDays(1), reservationTime, theme, waiter);

        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);
        memberRepository.save(waiter);
        reservationRepository.save(reservation);
        waitingRepsitory.save(waiting);
    }

    @Test
    @DisplayName("예약 전체 조회")
    void getReservations() throws Exception {
        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("예약 생성 테스트 - 인증 필요")
    void createReservation() throws Exception {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(2), 1L, 1L);
        String fakeJwtToken = jwtTokenProvider.createToken(member);
        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", fakeJwtToken))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("[실패] 예약 생성 테스트 - 중복된 예약")
    void createReservationAlreadyExists() throws Exception {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
        String fakeJwtToken = jwtTokenProvider.createToken(member);
        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("token", fakeJwtToken))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 삭제 테스트 - 예약 대기 자동으로 예약으로 변경")
    void deleteReservation() throws Exception {
        Long id = reservation.getId();
        // when & then
        mockMvc.perform(delete("/reservations/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/reservations"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].member.name").value("키링슬"));
    }

    @Test
    @DisplayName("내 예약 조회 - 인증 필요")
    void getMyReservations() throws Exception {
        //given
        String fakeJwtToken = jwtTokenProvider.createToken(member);
        // when & then
        mockMvc.perform(get("/reservations-mine")
                        .cookie(new Cookie("token", fakeJwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
} 
