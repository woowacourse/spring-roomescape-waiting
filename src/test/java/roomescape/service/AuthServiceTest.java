package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import roomescape.controller.dto.request.LoginRequest;
import roomescape.domain.member.Role;
import roomescape.security.TokenProvider;
import roomescape.service.dto.request.CreateMemberRequest;
import roomescape.service.dto.response.MemberResponse;

class AuthServiceTest extends BaseServiceTest {

    private static final String EMAIL = "auth@gmail.com";
    private static final String PASSWORD = "password";
    private static final String NICKNAME = "nickname";

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberService memberService;

    @SpyBean
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        CreateMemberRequest request = new CreateMemberRequest(EMAIL, PASSWORD, NICKNAME);
        memberService.createMember(request);
    }

    @Test
    @DisplayName("토큰을 생성한다.")
    void createToken() {
        doReturn("created_token").when(tokenProvider).createToken(any());

        String token = authService.createToken(1L);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(token).isEqualTo("created_token");
        });
    }

    @Test
    @DisplayName("비밀번호가 일치하는지 검증한다.")
    void validatePassword() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        MemberResponse response = authService.validatePassword(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.name()).isEqualTo(NICKNAME);
            softly.assertThat(response.role()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않을 경우 예외를 발생시킨다.")
    void validatePassword_fail_when_password_not_matched() {
        LoginRequest request = new LoginRequest(EMAIL, "wrong_password");

        assertThatThrownBy(() -> authService.validatePassword(request))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("등록되지 않은 이메일이거나 비밀번호가 틀렸습니다.");
    }

    @Test
    @DisplayName("토큰으로 회원 아이디를 가져올 수 있다.")
    void getMemberId() {
        doReturn(1L).when(tokenProvider).getMemberId(any());

        Long memberId = authService.getMemberIdByToken("token");

        assertThat(memberId).isEqualTo(1L);
    }
}
