package roomescape.controller;

import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Duration;
import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import roomescape.dto.UserInfo;
import roomescape.fixture.MemberFixture;
import roomescape.service.MemberService;
import roomescape.service.TokenService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MemberControllerTest {
    @MockBean
    private MemberService memberService;
    @MockBean
    private TokenService tokenService;
    @Autowired
    private MemberController memberController;

    @Test
    @DisplayName("로그인 기능 정상 동작 시 API 명세대로 응답이 생성되는지 확인")
    void login() {
        //given
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MDQwNDIwNjEsInVzZXJfaWQiOjF9.YT1w9--gkKwSRnQwi55bjfihQr5OrogCxSb65zDOutk";
        Long memberId = MemberFixture.DEFAULT_MEMBER.getId();

        Mockito.when(memberService.login(MemberFixture.DEFAULT_MEMBER_LOGIN_REQUEST))
                .thenReturn(memberId);
        Mockito.when(tokenService.createToken(Mockito.eq(memberId), Mockito.any(LocalDateTime.class),
                        Mockito.any(Duration.class)))
                .thenReturn(token);
        //when
        ResponseEntity<Void> response = memberController.login(MemberFixture.DEFAULT_MEMBER_LOGIN_REQUEST);
        //then
        assertAll(
                () -> Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200)),
                () -> {
                    String cookie = response.getHeaders().get(HttpHeaders.SET_COOKIE).get(0);
                    Assertions.assertThat(cookie)
                            .contains("token=" + token)
                            .contains("HttpOnly");
                }
        );
    }

    @Test
    @DisplayName("자기 정보 조회 정상 동작 시 API 명세대로 응답이 생성되는지 확인")
    void myInfo() {
        Mockito.when(memberService.findByUserId(MemberFixture.DEFAULT_MEMBER.getId()))
                .thenReturn(MemberFixture.DEFAULT_MEMBER_INFO);
        
        ResponseEntity<UserInfo> response = memberController.myInfo(MemberFixture.DEFAULT_MEMBER.getId());

        assertAll(
                () -> Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200)),
                () -> Assertions.assertThat(response.getBody()).isEqualTo(MemberFixture.DEFAULT_MEMBER_INFO)
        );
    }
}
