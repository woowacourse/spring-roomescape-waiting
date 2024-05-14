package roomescape.web.controller;


import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.service.MemberService;
import roomescape.service.security.JwtProvider;
import roomescape.web.dto.request.member.LoginRequest;
import roomescape.web.dto.response.member.MemberResponse;


@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MemberService memberService;
    @MockBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("로그인에 성공하면 200OK를 반환한다.")
    void login_ShouldReturn200StatusCode_WhenLoginSuccess() throws Exception {
        // given
        LoginRequest request = new LoginRequest("hello@world.kr", "password");
        ObjectMapper objectMapper = new ObjectMapper();

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("잘못된 형식의 이메일이 입력 되면 400BadRequest를 반환한다.")
    void login_ShouldReturn400StatusCode_WhenInsertInvalidEmailFormat() throws Exception {
        // given
        LoginRequest request = new LoginRequest("aaaa.aaa.aa", "password");
        ObjectMapper objectMapper = new ObjectMapper();
        Mockito.when(memberService.login(request))
                .thenReturn("");

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("이메일 또는 비밀번호를 형식에 맞춰 입력해주세요.")));
    }

    @Test
    @DisplayName("비밀번호가 빈값이면 400BadRequest를 반환한다.")
    void login_ShouldReturn400StatusCode_WhenInsertInvalidPasswordFormat() throws Exception {
        // given
        LoginRequest request = new LoginRequest("aaaa.aaa.aa", "  ");
        ObjectMapper objectMapper = new ObjectMapper();
        Mockito.when(memberService.login(request))
                .thenReturn("");

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("이메일 또는 비밀번호를 형식에 맞춰 입력해주세요.")));
    }

    @Test
    @DisplayName("토큰의 사용자 이름을 반환한다")
    void findAuthenticatedMember_ShouldReturnMemberName() throws Exception {
        // given
        Member member = new Member(1L, "name", "email@email.com", "password", Role.NORMAL);
        MemberResponse response = new MemberResponse(member.getId(), member.getName());
        String token = new JwtProvider().encode(member);
        Mockito.when(jwtProvider.extractName(any()))
                .thenReturn("name");
        Mockito.when(jwtProvider.extractId(any()))
                .thenReturn(1L);

        // when & then
        mockMvc.perform(get("/login/check")
                        .cookie(new Cookie("token", token)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

}
