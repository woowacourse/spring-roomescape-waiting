package roomescape.auth.controller;

import static java.util.Optional.empty;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.domain.Token;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.service.AuthService;
import roomescape.member.service.MemberService;
import roomescape.model.ControllerTest;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("쿠키를 잘 저장하는지 확인한다.")
    void saveCookie() throws Exception {
        when(authService.login(any()))
                .thenReturn(new Token("eocndxhzmsdlqslek"));

        String content = new ObjectMapper()
                .writeValueAsString(new LoginRequest("email", "password"));

        mockMvc.perform(post("/login")
                        .content(content)
                        .contentType("application/Json")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Set-Cookie", not(empty())));
    }
}
