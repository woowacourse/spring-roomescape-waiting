package roomescape.booking.waiting;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.JwtProvider;
import roomescape.auth.TokenBody;
import roomescape.booking.waiting.dto.WaitingResponse;
import roomescape.member.MemberRole;
import roomescape.member.dto.MemberResponse;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.schedule.dto.ScheduleResponse;
import roomescape.theme.dto.ThemeResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminWaitingController.class)
class AdminWaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private JwtProvider jwtProvider;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public WaitingService waitingService() {
            return mock(WaitingService.class);
        }

        @Bean
        public JwtProvider jwtProvider() {
            return mock(JwtProvider.class);
        }
    }

    @Test
    @DisplayName("관리자는 모든 대기를 조회할 수 있고, 성공 시 200을 응답한다")
    void readAll() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.ADMIN.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        MemberResponse memberResponse = new MemberResponse(1L, "사용자");
        ReservationTimeResponse timeResponse = new ReservationTimeResponse(1L, LocalTime.of(10, 0));
        ThemeResponse themeResponse = new ThemeResponse(1L, "테마명", "테마 설명", "http://example.com/thumbnail.jpg");
        ScheduleResponse scheduleResponse = new ScheduleResponse(1L, LocalDate.now(), timeResponse, themeResponse);
        List<WaitingResponse> responses = List.of(
                new WaitingResponse(1L, scheduleResponse, memberResponse, LocalDateTime.now()),
                new WaitingResponse(2L, scheduleResponse, memberResponse, LocalDateTime.now())
        );
        given(waitingService.readAll()).willReturn(responses);

        // when & then
        mockMvc.perform(get("/admin/waitings")
                        .cookie(new Cookie("token", "abc")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].member.id").value(1))
                .andExpect(jsonPath("$[0].member.name").value("사용자"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("대기가 없을 경우 빈 목록을 응답한다")
    void readAllEmpty() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.ADMIN.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        given(waitingService.readAll()).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/admin/waitings")
                        .cookie(new Cookie("token", "abc")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("일반 회원은 모든 대기를 조회할 수 없고, 401을 응답한다")
    void readAllUnauthorized() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.MEMBER.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        // when & then
        mockMvc.perform(get("/admin/waitings")
                        .cookie(new Cookie("token", "abc")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("관리자는 대기를 삭제할 수 있고, 성공 시 204를 응답한다")
    void deleteById() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.ADMIN.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        // when & then
        mockMvc.perform(delete("/admin/waitings/1")
                        .cookie(new Cookie("token", "abc")))
                .andExpect(status().isNoContent());

        verify(waitingService).deleteByIdForAdmin(1L);
    }

    @Test
    @DisplayName("일반 회원은 관리자 권한으로 대기를 삭제할 수 없고, 401을 응답한다")
    void deleteByIdUnauthorized() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.MEMBER.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        // when & then
        mockMvc.perform(delete("/admin/waitings/1")
                        .cookie(new Cookie("token", "abc")))
                .andExpect(status().isUnauthorized());
    }
}
