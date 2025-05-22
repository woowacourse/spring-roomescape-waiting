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
class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private WaitingRepsitory waitingRepsitory;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    ReservationTime reservationTime;
    Theme theme;
    Member reserver;
    Member waiter;
    Waiting waiting;

    @BeforeEach
    void beforeEach() {
        // 공통 예약 시간 및 테마 설정
        reservationTime = new ReservationTime(LocalTime.of(10, 0));
        theme = new Theme("테스트 테마", "설명", "썸네일");
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);

        // 예약자 A 등록 및 예약
        reserver = new Member(null, "예약자A", "reserver@email.com", "pw", Role.USER);
        memberRepository.save(reserver);
        Reservation reservation = new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, reserver);
        reservationRepository.save(reservation);

        Reservation reservation2 = new Reservation(LocalDate.now().plusDays(2), reservationTime, theme, reserver);
        reservationRepository.save(reservation2);

        // 대기자 B 등록 및 미리 대기 추가
        waiter = new Member(null, "대기자B", "waiter@email.com", "pw", Role.USER);
        memberRepository.save(waiter);
        waiting = new Waiting(null, LocalDate.now().plusDays(1), reservationTime, theme, waiter);
        waitingRepsitory.save(waiting);
    }

    @Test
    @DisplayName("대기 전체 조회 테스트")
    void findAllWaitings() throws Exception {
        // when & then
        mockMvc.perform(get("/waiting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("대기 생성 테스트 - 인증 필요")
    void createWaiting() throws Exception {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(2), 1L, 1L);
        String fakeJwtToken = jwtTokenProvider.createToken(waiter);
        // when & then
        mockMvc.perform(post("/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .cookie(new Cookie("token", fakeJwtToken)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("[실패] 대기 생성 테스트 - 중복 예약 불가")
    void createWaitingAlreadyReserved() throws Exception {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
        String fakeJwtToken = jwtTokenProvider.createToken(reserver);
        // when & then
        mockMvc.perform(post("/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .cookie(new Cookie("token", fakeJwtToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("대기 삭제 테스트")
    void deleteWaiting() throws Exception {
        // given
        Long id = waiting.getId();
        // when & then
        mockMvc.perform(delete("/waiting/" + id))
                .andExpect(status().isOk());
    }
}
