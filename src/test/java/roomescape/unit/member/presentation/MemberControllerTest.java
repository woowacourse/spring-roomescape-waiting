package roomescape.unit.member.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.auth.presentation.AuthorizationExtractor;
import roomescape.member.dto.request.MemberRequest;
import roomescape.member.dto.response.MemberResponse;
import roomescape.member.presentation.MemberController;
import roomescape.member.service.MemberService;

@WebMvcTest(value = {MemberController.class})
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private AuthorizationExtractor authorizationExtractor;

    @Test
    void 모든_회원을_조회한다() throws Exception {
        // given
        MemberResponse member = new MemberResponse(1L, "name1", "email1@domain.com");
        List<MemberResponse> response = List.of(member);
        given(memberService.findAllMembers()).willReturn(response);
        // when & then
        mockMvc.perform(get("/api/members")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    void 회원가입에_성공한다() throws Exception {
        // given
        MemberRequest request = new MemberRequest("email1@domain.com", "password1", "name1");
        MemberResponse response = new MemberResponse(1L, "name1", "email1@domain.com");
        given(memberService.createMember(request)).willReturn(response);
        // when & then
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }
}