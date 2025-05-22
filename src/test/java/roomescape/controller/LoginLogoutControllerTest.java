package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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
import roomescape.domain.enums.Role;
import roomescape.dto.login.LoginRequest;
import roomescape.repository.member.MemberRepository;
import roomescape.util.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class LoginLogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    private Member member;

    @BeforeEach
    void beforeEach() {
        member = new Member(null, "이름", "email", "password", Role.USER);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("로그인 기능 테스트")
    void userLogin() throws Exception {
        // given
        LoginRequest request = new LoginRequest("password", "email");
        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("token"));
    }

    @Test
    @DisplayName("로그인 체크 기능 테스트 - 인증 필요")
    void checkLogin() throws Exception {
        // when & then
        String fakeJwtToken = jwtTokenProvider.createToken(member);
        mockMvc.perform(get("/login/check").cookie(new Cookie("token", fakeJwtToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그아웃 기능 테스트")
    void logout() throws Exception {
        // when & then
        mockMvc.perform(post("/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("token"));
    }
} 
