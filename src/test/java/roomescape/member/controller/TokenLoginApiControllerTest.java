package roomescape.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.service.MemberService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TokenLoginApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    MemberService memberService;

    Map<String, String> member;

    @BeforeEach
    void setUp() {
        member = new HashMap<>();
        memberService.save(new SignUpRequest("매트", "matt@kakao.com", "1234"));
        member.put("name", "matt");
        member.put("email", "matt@kakao.com");
        member.put("password", "1234");
    }

    @Test
    @DisplayName("토큰 로그인에 성공한다.")
    void tokenLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(member)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원가입되지 않은 회원은 로그인에 실패한다.")
    void tokenLoginFailTest() throws Exception {
        Map<String, String> failMap = new HashMap<>(
                Map.of(
                        "name", "말론", "email", "malone.gmail", "password", "1234"
                )
        );
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failMap)))
                .andExpect(status().isBadRequest());
    }
}
