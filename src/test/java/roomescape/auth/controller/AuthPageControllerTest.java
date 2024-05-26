package roomescape.auth.controller;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import roomescape.util.ControllerTest;

@DisplayName("회원 페이지 통합 테스트")
class AuthPageControllerTest extends ControllerTest {

    @DisplayName("로그인 페이지 조회 시, 200을 반환한다.")
    @Test
    void getLoginPage() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/login")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("회원가입 페이지 조회 시, 200을 반환한다.")
    @Test
    void getSignupPage() throws Exception {
        //given & when & then
        mockMvc.perform(
                get("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}
