package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;
import roomescape.CurrentDateTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationCreateCommand;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.dto.WaitingAddCommand;

@ActiveProfiles({"test", "auth"})
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class ReservationWaitingMockMvcSpringBootTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ReservationService reservationService;
    @Autowired
    WaitingService waitingService;
    @Autowired
    ReservationRepository reservationRepository;
    @MockitoBean
    CurrentDateTime currentDateTime;
    LocalDate today;
    ReservationInfo savedReservationInfo;

    @BeforeEach
    void init() {
        today = LocalDate.of(2025, 4, 28);
        when(currentDateTime.getTime()).thenReturn(LocalTime.of(16, 1));
        when(currentDateTime.getDate()).thenReturn(today);
        savedReservationInfo = reservationService.createReservation(
                new ReservationCreateCommand(today.plusDays(1), 2L, 2L, 11L));
        waitingService.addWaiting(new WaitingAddCommand(today.plusDays(1), 1L, 11L, 2L));
        waitingService.addWaiting(new WaitingAddCommand(today.plusDays(2), 1L, 11L, 2L));
    }

    @DisplayName("내 예약과 대기 목록을 조회할 수 있다")
    @Test
    void aa() throws Exception {
        // given
        String userToken = getUserToken();
        Cookie cookie = new Cookie("token", userToken);

        // when
        // then
        mockMvc.perform(get("/reservations-mine").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @DisplayName("예약 취소 시 순번이 가장 빠른 예약 대기가 예약된다")
    @Test
    void aaa() throws Exception {
        // given
        String adminToken = getAdminToken();
        Cookie adminCookie = new Cookie("token", adminToken);
        Reservation reservation = reservationRepository.findAllByCondition(1L, 11L, today.plusDays(1),
                today.plusDays(1)).getFirst();

        // when
        mockMvc.perform(delete("/reservations/" + reservation.getId()).cookie(adminCookie));

        // then
        List<Reservation> reservations = reservationRepository.findAllByMemberId(2L);
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("예약에 대한 취소 권한이 없을 시 예외가 발생한다")
    @Test
    void aaaa() throws Exception {
        // given
        String userToken = getUserToken();
        Cookie userCookie = new Cookie("token", userToken);
        Reservation reservation = reservationRepository.findAllByCondition(1L, 11L, today.plusDays(1),
                today.plusDays(1)).getFirst();

        // when
        // then
        mockMvc.perform(delete("/reservations/" + reservation.getId()).cookie(userCookie))
                .andExpect(status().isForbidden());
    }

    private String getAdminToken() throws Exception {
        return mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content("""
                                {
                                    "email": "admin@gmail.com",
                                    "password": "qwer!"
                                }
                                """)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("token")
                .getValue();
    }

    private String getUserToken() throws Exception {
        return mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content("""
                                {
                                    "email": "user@gmail.com",
                                    "password": "qwer!"
                                }
                                """)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("token")
                .getValue();
    }
}
