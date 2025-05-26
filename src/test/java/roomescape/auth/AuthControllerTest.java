package roomescape.auth;

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
import roomescape.auth.dto.LoginMember;
import roomescape.auth.dto.LoginRequest;
import roomescape.exception.custom.reason.auth.AuthNotExistsEmailException;
import roomescape.member.MemberRole;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProvider jwtProvider;

    @TestConfiguration
    static class TestConfig {

        @Bean
        AuthService authService() {
            return mock(AuthService.class);
        }

        @Bean
        JwtProvider jwtProvider() {
            return mock(JwtProvider.class);
        }
    }

    @Test
    @DisplayName("로그인에 성공하면 토큰이 담긴 쿠키를 응답한다")
    void login1() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "1234");
        String token = "imtoken!";
        given(authService.generateToken(loginRequest))
                .willReturn(token);

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(header().string("Set-Cookie", "token=" + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인에 실패하면 400을 응답한다")
    void login2() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "1234");
        given(authService.generateToken(loginRequest))
                .willThrow(new AuthNotExistsEmailException());

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("현재 로그인한 회원의 정보를 확인할 수 있다")
    void loginCheck1() throws Exception {
        // given
        LoginMember loginMember = new LoginMember("may", "may@gmail.com", MemberRole.MEMBER);
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("name", loginMember.name());
        memberClaims.put("email", loginMember.email());
        memberClaims.put("role", loginMember.role().name());

        Claims claims = Jwts.claims();
        claims.putAll(memberClaims);

        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        // when & then
        mockMvc.perform(get("/login/check")
                        .cookie(new Cookie("token", "abc123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(loginMember.name()));
    }

    @Test
    @DisplayName("로그인 정보가 올바르지 않으면 400을 응답한다.")
    void loginCheck2() throws Exception {
        // given
        LoginMember loginMember = new LoginMember("may", "may@gmail.com", MemberRole.MEMBER);
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("name", loginMember.name());
        memberClaims.put("email", loginMember.email());
        memberClaims.put("role", loginMember.role().name());

        Claims claims = Jwts.claims();
        claims.putAll(memberClaims);

        given(jwtProvider.isValidToken(any()))
                .willReturn(false);

        // when & then
        mockMvc.perform(get("/login/check")
                        .cookie(new Cookie("token", "abc123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 요청을 하면 쿠키에 빈 토큰이 반환된다")
    void logout() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(header().string("Set-Cookie", "token="))
                .andExpect(status().isOk());
    }
}
