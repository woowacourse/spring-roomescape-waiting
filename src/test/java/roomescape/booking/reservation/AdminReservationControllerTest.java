package roomescape.booking.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.JwtProvider;
import roomescape.auth.TokenBody;
import roomescape.booking.reservation.dto.AdminFilterReservationRequest;
import roomescape.booking.reservation.dto.AdminReservationRequest;
import roomescape.booking.reservation.dto.ReservationResponse;
import roomescape.member.MemberRole;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationCreateService reservationCreateService;

    @Autowired
    private JwtProvider jwtProvider;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ReservationService reservationService() {
            return mock(ReservationService.class);
        }

        @Bean
        public ReservationCreateService reservationCreateService() {
            return mock(ReservationCreateService.class);
        }

        @Bean
        public JwtProvider jwtProvider() {
            return mock(JwtProvider.class);
        }
    }

    @Test
    @DisplayName("관리자는 특정 회원의 예약을 생성할 수 있고, 성공 시 201을 응답한다.")
    void createReservation1() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.ADMIN.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 1L, 1L);
        ReservationResponse response = new ReservationResponse(1L, null, null);
        given(reservationCreateService.createForAdmin(request))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .cookie(new Cookie("token", "abc"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("일반 회원은 특정 회원의 예약을 생성할 수 없고, 401을 응답한다.")
    void createReservation2() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.MEMBER.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 1L, 1L);

        // when & then
        mockMvc.perform(post("/admin/reservations")
                        .cookie(new Cookie("token", "abc"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("관리자는 회원, 테마, 날짜 기반 필터링된 예약을 확인할 수 있다.")
    void readAllByMemberAndThemeAndDateRange1() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.ADMIN.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        AdminFilterReservationRequest request = new AdminFilterReservationRequest(1L, 1L, LocalDate.now().minusDays(1), LocalDate.now());
        given(reservationService.getAllByMemberAndThemeAndDateRange(request))
                .willReturn(any());

        // when & then
        mockMvc.perform(get("/admin/reservations")
                        .cookie(new Cookie("token", "abc"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일반 회원은 필터링된 예약을 확인할 수 없고, 401을 응답한다.")
    void readAllByMemberAndThemeAndDateRange2() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.MEMBER.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 1L, 1L);

        // when & then
        mockMvc.perform(get("/admin/reservations")
                        .cookie(new Cookie("token", "abc"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
