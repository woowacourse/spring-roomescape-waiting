package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.common.exception.InvalidInputException;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.dto.request.LoginRequestDto;
import roomescape.dao.MemberDao;
import roomescape.service.MemberService;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    private final Member member = new Member(1L, "유저", "user@test.com", "password", MemberRole.USER);

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private MemberDao memberDao;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Nested
    class Login {

        @Test
        @DisplayName("올바른 이메일과 비밀번호로 로그인하면 200을 반환한다")
        void returnsOkOnValidCredentials() {
            given(memberService.login(any())).willReturn(member);
            LoginRequestDto request = new LoginRequestDto("user@test.com", "password");

            RestAssuredMockMvc.given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(request)
                    .when().post("/login")
                    .then()
                    .status(HttpStatus.OK);
        }

        @Test
        @DisplayName("잘못된 이메일 또는 비밀번호로 로그인하면 400을 반환한다")
        void returnsBadRequestOnInvalidCredentials() {
            willThrow(new InvalidInputException("이메일 또는 비밀번호가 올바르지 않습니다."))
                    .given(memberService).login(any());
            LoginRequestDto request = new LoginRequestDto("user@test.com", "wrong");

            RestAssuredMockMvc.given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(request)
                    .when().post("/login")
                    .then()
                    .status(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("이메일 또는 비밀번호가 비어있으면 400을 반환한다")
        void returnsBadRequestOnBlankFields() {
            LoginRequestDto request = new LoginRequestDto("", "");

            RestAssuredMockMvc.given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(request)
                    .when().post("/login")
                    .then()
                    .status(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class Logout {

        @Test
        @DisplayName("로그아웃하면 200을 반환한다")
        void returnsOkOnLogout() {
            RestAssuredMockMvc.given()
                    .when().post("/logout")
                    .then()
                    .status(HttpStatus.OK);
        }
    }
}
