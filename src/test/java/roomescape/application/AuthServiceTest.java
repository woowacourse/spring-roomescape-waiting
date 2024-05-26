package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.request.LoginRequest;
import roomescape.application.dto.request.SignupRequest;
import roomescape.application.dto.response.MemberResponse;
import roomescape.domain.member.Role;
import roomescape.exception.BadRequestException;

class AuthServiceTest extends BaseServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("토큰을 생성한다.")
    void createToken() {
        String token = authService.createToken(1L);

        assertThatCode(() -> tokenProvider.getMemberId(token))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("비밀번호가 일치하는지 검증한다.")
    void validatePassword() {
        memberService.createMember(new SignupRequest("ex@gmail.com", "password", "구름"));

        LoginRequest request = new LoginRequest("ex@gmail.com", "password");

        MemberResponse response = authService.validatePassword(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.email()).isEqualTo("ex@gmail.com");
            softly.assertThat(response.name()).isEqualTo("구름");
            softly.assertThat(response.role()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않을 경우 예외를 발생시킨다.")
    void validatePasswordWhenPasswordIsNotMatch() {
        memberService.createMember(new SignupRequest("ex@gmail.com", "password", "구름"));

        LoginRequest request = new LoginRequest("ex@gmail.com", "wrong_password");

        assertThatThrownBy(() -> authService.validatePassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("토큰으로 회원 아이디를 가져올 수 있다.")
    void getMemberId() {
        Long memberId = 1L;

        String token = tokenProvider.createToken(memberId.toString());
        Long memberIdByToken = authService.getMemberIdByToken(token);

        assertThat(memberIdByToken).isEqualTo(memberId);
    }
}
