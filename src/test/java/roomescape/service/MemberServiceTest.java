package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Password;
import roomescape.domain.PasswordEncoder;
import roomescape.domain.dto.LoginRequest;
import roomescape.domain.dto.SignupRequest;
import roomescape.domain.dto.TokenResponse;
import roomescape.exception.SignupFailException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @InjectMocks
    private final MemberService service;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    public MemberServiceTest(final MemberService service) {
        this.service = service;
    }

    private long getMemberSize() {
        return service.findEntireMembers().getData().size();
    }

    @Test
    @DisplayName("사용자 목록을 반환한다.")
    void given_when_findEntireMembers_then_returnMemberResponses() {
        assertThat(service.findEntireMembers().getData().size()).isEqualTo(3);
    }

    @DisplayName("회원정보가 등록되어 있지않으면 회원가입을 성공한다.")
    @Test
    void given_signupRequest_when_registerUser_then_success() {
        //given
        when(passwordEncoder.encode(any(String.class))).thenReturn(new Password("hashedpassword", "salt"));
        SignupRequest signupRequest = new SignupRequest("ash@test.com", "123456", "ash");
        //when
        service.registerUser(signupRequest);
        //then
        assertThat(getMemberSize()).isEqualTo(4);
    }

    @DisplayName("이미 회원으로 등록되어 있으면 회원을 가입하지 않는다.")
    @Test
    void given_signupRequest_when_registerUserWithAlreadyExistMember_then_fail() {
        //given
        when(passwordEncoder.encode(any(String.class), any(String.class))).thenReturn(
                new Password("hashedpassword", "salt"));
        SignupRequest signupRequest = new SignupRequest("user@test.com", "123456", "ash");
        //when, then
        assertAll(
                () -> assertThatThrownBy(() -> service.registerUser(signupRequest)).isInstanceOf(SignupFailException.class),
                () -> assertThat(getMemberSize()).isEqualTo(3)
        );
    }

    @DisplayName("로그인 정보가 일치하면 토큰을 반환한다.")
    @Test
    void given_LoginRequest_when_loginSuccess_then_doesNotAnyException() {
        //given
        when(passwordEncoder.encode(any(String.class), any(String.class))).thenReturn(
                new Password("hashedpassword", "salt"));
        LoginRequest loginRequest = new LoginRequest("user@test.com", "password");
        //when
        var response = service.login(loginRequest);
        //then
        assertThat(response).isInstanceOf(TokenResponse.class);
    }
}
