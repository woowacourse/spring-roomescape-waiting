package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.reservation.fixture.ReservationDateFixture.예약날짜_내일;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    EntityManager em;

    private Theme theme;
    private Member member;
    private Map<String, Object> reservation;
    private Cookie tokenCookie;
    private ReservationTime reservationTime;

    @BeforeEach
    void setUp() {
        theme = themeRepository.save(new Theme("테마1", "설명1", "썸네일1"));
        member = memberRepository.save(
                new Member(new Name("매트"), new Email("matt@kakao.com"), new Password("1234"), Role.ADMIN));
        reservationTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        String token = authService.loginByToken(new TokenLoginCreateRequest("matt@kakao.com", "1234")).tokenResponse();
        tokenCookie = new Cookie("token", token);

        reservation = new HashMap<>();
        reservation.put("memberId", member.getId());
        reservation.put("date", "2025-08-05");
        reservation.put("timeId", reservationTime.getId());
        reservation.put("themeId", theme.getId());
    }

    @Test
    void 방탈출_예약을_생성_조회_삭제한다() throws Exception {
        // 예약 생성
        mockMvc.perform(post("/admin/reservations")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated());

        // 예약 조회
        mockMvc.perform(get("/reservations")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // 생성된 예약 ID 찾기
        Reservation createdReservation = reservationRepository.findByThemeIdAndReservationTimeIdAndReservationDate_reservationDate(
                theme.getId(), 
                reservationTime.getId(), 
                LocalDate.parse((String) reservation.get("date")))
                .orElseThrow();

        // 예약 삭제
        mockMvc.perform(delete("/admin/reservations/" + createdReservation.getId())
                        .cookie(tokenCookie))
                .andExpect(status().isNoContent());

        // 삭제 후 조회
        mockMvc.perform(get("/reservations")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void 예약_시간을_조회_삭제한다() throws Exception {
        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/times/" + reservationTime.getId())
                        .cookie(tokenCookie))
                .andExpect(status().isNoContent());
    }

    @Test
    void 관리자_페이지를_응답한다() throws Exception {
        mockMvc.perform(get("/admin")
                        .cookie(tokenCookie))
                .andExpect(status().isOk());
    }

    @Test
    void 방탈출_예약_페이지를_응답한다() throws Exception {
        mockMvc.perform(get("/admin/reservation")
                        .cookie(tokenCookie))
                .andExpect(status().isOk());
    }

    @Test
    void 방탈출_예약_목록을_응답한다() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void 예약_삭제시_존재하지_않는_예약이면_예외를_응답한다() throws Exception {
        mockMvc.perform(delete("/admin/reservations/999")
                        .cookie(tokenCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_시간_삭제시_존재하지_않는_예약시간이면_예외를_응답한다() throws Exception {
        mockMvc.perform(delete("/times/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 방탈출_예약_목록을_조회한다() throws Exception {
        reservationRepository.save(
                Reservation.create(예약날짜_내일.getDate(), reservationTime, theme, member));

        String response = mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ReservationResponse> reservations = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ReservationResponse.class));
        assertThat(reservations).hasSize(1);
    }

    @Test
    void 방탈출_예약_목록을_생성_조회_삭제한다() throws Exception {
        // 예약 생성
        mockMvc.perform(post("/admin/reservations")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated());

        // 생성된 예약 ID 찾기
        Reservation createdReservation = reservationRepository.findByThemeIdAndReservationTimeIdAndReservationDate_reservationDate(
                theme.getId(), 
                reservationTime.getId(), 
                LocalDate.parse((String) reservation.get("date")))
                .orElseThrow();

        // 예약 삭제
        mockMvc.perform(delete("/admin/reservations/" + createdReservation.getId())
                        .cookie(tokenCookie))
                .andExpect(status().isNoContent());
    }

    @Test
    void 날짜가_null이면_예외를_응답한다() throws Exception {
        Map<String, Object> reservationFail = new HashMap<>();
        reservationFail.put("name", "브라운");
        reservationFail.put("timeId", 1);
        reservationFail.put("themeId", 1);

        mockMvc.perform(post("/admin/reservations")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationFail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 시간이_null이면_예외를_응답한다() throws Exception {
        Map<String, String> reservationFail = new HashMap<>();
        reservationFail.put("startAt", "");

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationFail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 멤버의_예약목록을_가져온다() throws Exception {
        // 예약 생성
        mockMvc.perform(post("/admin/reservations")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated());

        // 예약 목록 조회
        mockMvc.perform(get("/reservations-mine")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
