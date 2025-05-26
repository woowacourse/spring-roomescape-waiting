package roomescape.booking.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
import roomescape.auth.dto.LoginMember;
import roomescape.booking.BookingService;
import roomescape.booking.reservation.dto.ReservationRequest;
import roomescape.booking.reservation.dto.ReservationResponse;
import roomescape.member.MemberRole;
import roomescape.member.dto.MemberResponse;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.schedule.dto.ScheduleResponse;
import roomescape.theme.dto.ThemeResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationCreateService reservationCreateService;

    @Autowired
    private BookingService bookingService;

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
        public BookingService bookingService() {
            return mock(BookingService.class);
        }

        @Bean
        public JwtProvider jwtProvider() {
            return mock(JwtProvider.class);
        }
    }

    @Test
    @DisplayName("예약 생성 요청에 성공할 경우 201을 응답한다")
    void create() throws Exception {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.now(), 1L, 1L);
        LoginMember loginMember = new LoginMember("사용자", "user@example.com", MemberRole.MEMBER);

        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("name", loginMember.name());
        memberClaims.put("email", loginMember.email());
        memberClaims.put("role", loginMember.role().name());
        Claims claims = Jwts.claims();
        claims.putAll(memberClaims);

        given(jwtProvider.isValidToken(any())).willReturn(true);
        given(jwtProvider.extractBody(any())).willReturn(new TokenBody(claims));

        MemberResponse memberResponse = new MemberResponse(1L, loginMember.name());
        ReservationTimeResponse timeResponse = new ReservationTimeResponse(1L, LocalTime.of(10, 0));
        ThemeResponse themeResponse = new ThemeResponse(1L, "테마명", "테마 설명", "abc");
        ScheduleResponse scheduleResponse = new ScheduleResponse(1L, LocalDate.now(), timeResponse, themeResponse);
        ReservationResponse response = new ReservationResponse(1L, scheduleResponse, memberResponse);

        given(reservationCreateService.create(any(ReservationRequest.class), any(LoginMember.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/reservations")
                        .cookie(new Cookie("token", "abc123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.member.id").value(1))
                .andExpect(jsonPath("$.member.name").value(loginMember.name()))
                .andExpect(jsonPath("$.schedule.id").value(1));
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 예약 생성 요청은 401을 응답한다")
    void createUnauthorized() throws Exception {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.now(), 1L, 1L);
        given(jwtProvider.isValidToken(any())).willReturn(false);

        // when & then
        mockMvc.perform(post("/reservations")
                        .cookie(new Cookie("token", "invalid"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("예약 삭제 요청에 성공할 경우 204를 응답한다")
    void deleteById() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isNoContent());

        verify(bookingService).deleteReservationById(1L);
    }
}
