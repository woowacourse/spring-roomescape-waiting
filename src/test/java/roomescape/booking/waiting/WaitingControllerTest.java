package roomescape.booking.waiting;

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
import roomescape.booking.waiting.dto.WaitingRequest;
import roomescape.booking.waiting.dto.WaitingResponse;
import roomescape.member.MemberRole;
import roomescape.member.dto.MemberResponse;
import roomescape.schedule.dto.ScheduleResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WaitingController.class)
class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private WaitingCreateService waitingCreateService;

    @Autowired
    private JwtProvider jwtProvider;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public WaitingService waitingService() {
            return mock(WaitingService.class);
        }

        @Bean
        public WaitingCreateService waitingCreateService() {
            return mock(WaitingCreateService.class);
        }

        @Bean
        public JwtProvider jwtProvider() {
            return mock(JwtProvider.class);
        }
    }

    @Test
    @DisplayName("대기 생성 요청에 성공할 경우 201을 응답한다")
    void create() throws Exception {
        // given
        WaitingRequest request = new WaitingRequest(LocalDate.now(), 1L, 1L);
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
        ScheduleResponse scheduleResponse = new ScheduleResponse(1L, null, null, null);
        WaitingResponse response = new WaitingResponse(1L, scheduleResponse, memberResponse, LocalDateTime.now());

        given(waitingCreateService.create(any(WaitingRequest.class), any(LoginMember.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/waitings")
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
    @DisplayName("인증되지 않은 사용자의 대기 생성 요청은 401을 응답한다")
    void createUnauthorized() throws Exception {
        // given
        WaitingRequest request = new WaitingRequest(LocalDate.now(), 1L, 1L);
        given(jwtProvider.isValidToken(any())).willReturn(false);

        // when & then
        mockMvc.perform(post("/waitings")
                        .cookie(new Cookie("token", "invalid"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("대기 삭제 요청에 성공할 경우 204를 응답한다")
    void deleteById() throws Exception {
        // given
        LoginMember loginMember = new LoginMember("사용자", "user@example.com", MemberRole.MEMBER);

        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("name", loginMember.name());
        memberClaims.put("email", loginMember.email());
        memberClaims.put("role", loginMember.role().name());
        Claims claims = Jwts.claims();
        claims.putAll(memberClaims);

        given(jwtProvider.isValidToken(any())).willReturn(true);
        given(jwtProvider.extractBody(any())).willReturn(new TokenBody(claims));

        // when & then
        mockMvc.perform(delete("/waitings/1")
                        .cookie(new Cookie("token", "abc123")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 대기 삭제 요청은 401을 응답한다")
    void deleteByIdUnauthorized() throws Exception {
        // given
        given(jwtProvider.isValidToken(any())).willReturn(false);

        // when & then
        mockMvc.perform(delete("/waitings/1")
                        .cookie(new Cookie("token", "invalid")))
                .andExpect(status().isUnauthorized());
    }
}
