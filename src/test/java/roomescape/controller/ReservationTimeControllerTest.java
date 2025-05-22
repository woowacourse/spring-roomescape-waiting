package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    ReservationTime reservationTime;
    ReservationTime reservationTime2;
    Theme theme;
    Member member;
    Reservation reservation;

    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void beforeEach() {
        reservationTime = new ReservationTime(LocalTime.of(10, 0));
        reservationTime2 = new ReservationTime(LocalTime.of(11, 0));
        theme = new Theme("이름", "설명", "썸네일");
        member = new Member(null, "슬링키", "email", "password", Role.USER);
        reservation = new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, member);

        reservationTimeRepository.save(reservationTime);
        reservationTimeRepository.save(reservationTime2);
        themeRepository.save(theme);
        memberRepository.save(member);
        reservationRepository.save(reservation);
    }

    @Test
    @DisplayName("예약 시간 생성 테스트")
    void createReservationTime() throws Exception {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(12, 0));

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startAt").value("12:00:00"));
    }

    @Test
    @DisplayName("예약 시간 전체 조회 테스트")
    void getReservationTimes() throws Exception {

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0,0));
        reservationTimeRepository.save(reservationTime);

        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").exists());
    }

    @Test
    @DisplayName("예약 시간 삭제 테스트 - 예약 내역이 없는 경우")
    void deleteReservationTime() throws Exception {

        ReservationTimeRequest request = new ReservationTimeRequest(reservationTime2.getStartAt());

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = reservationTime2.getId();

        mockMvc.perform(delete("/times/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("예약 시간 삭제 테스트 - 예약 내역이 있는 경우")
    void deleteReservationTimeReservatinoExitst() throws Exception {

        ReservationTimeRequest request = new ReservationTimeRequest(reservationTime.getStartAt());

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = reservationTime.getId();

        mockMvc.perform(delete("/times/" + id))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 가능한 시간 조회 테스트")
    void getAvailableReservationTimes() throws Exception {

        mockMvc.perform(get("/times/available")
                        .param("date", reservation.getDate().toString())
                        .param("themeId", theme.getId().toString()))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].alreadyBooked").value(true))
                .andExpect(jsonPath("$[1].alreadyBooked").value(false));
    }
}
