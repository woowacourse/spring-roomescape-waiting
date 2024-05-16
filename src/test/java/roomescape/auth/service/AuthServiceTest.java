package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static roomescape.auth.fixture.TokenFixture.DUMMY_TOKEN;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.dto.Accessor;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.global.exception.NoSuchRecordException;
import roomescape.global.exception.auth.WrongPasswordException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.fixture.MemberFixture;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MemberRepository memberRepository;


    @DisplayName("로그인 요청에 성공하면 토큰을 반환한다")
    @Test
    void should_return_token_when_valid_login_request_arrived() {
        when(memberRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(MemberFixture.MEMBER_ID_1));
        when(jwtTokenProvider.createToken(any(Member.class))).thenReturn(DUMMY_TOKEN);

        LoginRequest request = new LoginRequest("andole@test.com", "123");
        assertThat(authService.login(request).getAccessToken()).isNotNull();
    }

    @DisplayName("로그인 요청 시 이메일에 해당하는 유저를 찾을 수 없을 경우 예외가 발생한다")
    @Test
    void should_throw_exception_when_email_not_exist() {
        when(memberRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest("noSignedUp@gmail.com", "123");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(NoSuchRecordException.class);
    }

    @DisplayName("로그인 요청 시 비밀번호가 틀리다면 예외가 발생한다")
    @Test
    void should_throw_exception_when_password_is_incorrect() {
        when(memberRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(MemberFixture.MEMBER_ID_1));
        LoginRequest request = new LoginRequest("aa@gmail.com", "1234");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(WrongPasswordException.class);
    }

    @DisplayName("로그인 체크 시 인증 정보에 해당하는 이름을 반환한다")
    @Test
    void should_return_name_response_when_valid_token_check_performed() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(MemberFixture.MEMBER_ID_1));

        assertThat(authService.checkLogin(new Accessor(1L)).name()).isEqualTo("안돌");
    }

    @DisplayName("토큰에서 유저의 아이디를 추출했을 때 그 아이디에 해당하는 회원이 없으면 예외를 발생시킨다")
    @Test
    void should_throw_exception_when_token_member_no_exist() {
        when(memberRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.checkLogin(new Accessor(1L)))
                .isInstanceOf(NoSuchRecordException.class);
    }
}
