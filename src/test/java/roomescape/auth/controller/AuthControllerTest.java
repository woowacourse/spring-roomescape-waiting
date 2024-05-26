package roomescape.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.fixture.MemberFixture.getMemberChoco;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import roomescape.auth.controller.dto.MemberResponse;
import roomescape.auth.controller.dto.TokenResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;
import roomescape.util.ControllerTest;

@DisplayName("회원 API 통합 테스트")
class AuthControllerTest extends ControllerTest {

    @DisplayName("로그인에 성공할 경우, 200을 반환한다.")
    @Test
    void login() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        TokenResponse tokenResponse = new TokenResponse("accessToken");

        Map<String, String> params = new HashMap<>();
        params.put("email", getMemberChoco().getEmail());
        params.put("password", getMemberChoco().getPassword());

        //when
        doReturn(memberResponse)
                .when(authService)
                .signUp(any());

        doReturn(tokenResponse)
                .when(authService)
                .createToken(any());

        //then
        mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isOk());
    }

    @DisplayName("토큰을 통해 회원을 검증할 경우, 200을 반환한다.")
    @Test
    void loginCheck() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/login/check")
                        .cookie(new Cookie("token", memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("토큰을 통해 로그아웃할 경우, 200을 반환한다.")
    @Test
    void logout() throws Exception {
        //given & when & then
        mockMvc.perform(
                        post("/logout")
                                .cookie(new Cookie("token", memberToken))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(header().doesNotExist("token"));
    }

    @DisplayName("회원 가입 시, 201을 반환한다.")
    @Test
    void signup() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");

        Map<String, String> params = new HashMap<>();
        params.put("name", "chocochip2");
        params.put("email", "dev.chocochip2@gmail.com");
        params.put("password", "12345");

        //when
        doReturn(memberResponse)
                .when(authService)
                .signUp(any());

        //then
        mockMvc.perform(
                post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isCreated());
    }

    @DisplayName("중복된 메일로 회원 가입할 경우, 400를 반환한다.")
    @Test
    void duplicatedEmail() throws Exception {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("name", "chocochip");
        params.put("email", "dev.chocochip@gmail.com");
        params.put("password", "12345");

        //when
        doThrow(new BadRequestException(ErrorType.DUPLICATED_EMAIL_ERROR))
                .when(authService)
                .signUp(any());

        //then
        mockMvc.perform(
                post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("메일 형식이 아닐 경우, 400을 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "chocochip", "chocochip@"})
    void signupInvalidMailFormat(String invalidMail) throws Exception {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("name", "chocochip");
        params.put("email", invalidMail);
        params.put("password", "12345");

        //when & then
        mockMvc.perform(
                post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isBadRequest());
    }
}
