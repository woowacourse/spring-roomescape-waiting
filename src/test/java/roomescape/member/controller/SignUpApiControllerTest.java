package roomescape.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SignUpApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, String> member;

    @BeforeEach
    void setUp() {
        member = new HashMap<>();
        member.put("name", "matt");
        member.put("email", "matt@kakao.com");
        member.put("password", "1234");
    }

    @Test
    @DisplayName("회원 가입을 진행한다.")
    void signUpTest() throws Exception {
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(member)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("이름이 공백이면 회원 가입에 실패한다.")
    void nameNullSignUpTest() throws Exception {
        Map<String, String> failMap = new HashMap<>(
                Map.of(
                        "name", "", "email", "matt.kakao", "password", "1234"
                )
        );

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failMap)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일이 공백이면 회원 가입에 실패한다.")
    void emailNullSignUpTest() throws Exception {
        Map<String, String> failMap = new HashMap<>(
                Map.of(
                        "name", "matt", "email", "", "password", "1234"
                )
        );

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failMap)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호가 공백이면 회원 가입에 실패한다.")
    void passwordNullSignUpTest() throws Exception {
        Map<String, String> failMap = new HashMap<>(
                Map.of(
                        "name", "matt", "email", "matt.kakao", "password", ""
                )
        );

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failMap)))
                .andExpect(status().isBadRequest());
    }
}
