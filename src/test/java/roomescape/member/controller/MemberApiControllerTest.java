package roomescape.member.controller;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.config.ControllerConfig;
import roomescape.member.dto.MemberSignUpRequest;
import roomescape.member.service.MemberService;

@WebMvcTest(MemberApiController.class)
@Import(ControllerConfig.class)
class MemberApiControllerTest {

    @MockBean
    private MemberService memberService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입에 성공하면 201 응답과 Location 헤더에 리소스 저장 경로를 받는다.")
    void signup() throws Exception {
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(
                "호기", "hogi@naver.com", "asd");

        doReturn(1L).when(memberService)
                .save(memberSignUpRequest);

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberSignUpRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/members/1"));
    }

    @Test
    @DisplayName("회원 목록 조회에 성공하면 200 응답을 받는다.")
    void findAll() throws Exception {
        mockMvc.perform(get("/members")
                        .cookie(new Cookie("token", "1234"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
