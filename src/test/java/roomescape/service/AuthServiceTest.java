package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.controller.dto.SignupRequest;
import roomescape.repository.MemberDao;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberDao memberDao;

    @InjectMocks
    private AuthService authService;

    @DisplayName("로그인 ID와 비밀번호가 일치하면 회원을 반환한다.")
    @Test
    void login() {
        Member member = new Member(1L, "roro", "러로", "password", Role.USER);
        given(memberDao.findByLoginId("roro")).willReturn(Optional.of(member));

        Member loginMember = authService.login("roro", "password");

        assertThat(loginMember).isEqualTo(member);
    }

    @DisplayName("로그인 ID가 없거나 비밀번호가 다르면 예외를 던진다.")
    @Test
    void invalidLogin() {
        Member member = new Member(1L, "roro", "러로", "password", Role.USER);
        given(memberDao.findByLoginId("unknown")).willReturn(Optional.empty());
        given(memberDao.findByLoginId("roro")).willReturn(Optional.of(member));

        assertRoomescapeException(() -> authService.login("unknown", "password"), DomainErrorCode.INVALID_LOGIN);
        assertRoomescapeException(() -> authService.login("roro", "wrong"), DomainErrorCode.INVALID_LOGIN);
    }

    @DisplayName("세션의 회원 ID로 로그인 회원을 조회한다.")
    @Test
    void getLoginMember() {
        Member member = new Member(1L, "roro", "러로", "password", Role.USER);
        given(memberDao.findById(1L)).willReturn(Optional.of(member));

        assertThat(authService.getLoginMember(1L)).isEqualTo(member);
    }

    @DisplayName("세션의 회원 ID가 유효하지 않으면 인증 예외를 던진다.")
    @Test
    void getLoginMemberNotFound() {
        given(memberDao.findById(1L)).willReturn(Optional.empty());

        assertRoomescapeException(() -> authService.getLoginMember(1L), DomainErrorCode.UNAUTHENTICATED);
    }

    @DisplayName("회원가입하면 USER 권한 회원을 저장하고 반환한다.")
    @Test
    void signup() {
        SignupRequest request = new SignupRequest("새회원", "new-user", "password", "password");
        Member saved = new Member(1L, "new-user", "새회원", "password", Role.USER);
        given(memberDao.existsByLoginId("new-user")).willReturn(false);
        given(memberDao.save(any(Member.class))).willReturn(1L);
        given(memberDao.findById(1L)).willReturn(Optional.of(saved));

        Member member = authService.signup(request);

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getRole()).isEqualTo(Role.USER);
    }

    @DisplayName("회원가입 비밀번호 확인이 다르면 예외를 던진다.")
    @Test
    void signupPasswordMismatch() {
        SignupRequest request = new SignupRequest("새회원", "new-user", "password", "wrong");

        assertRoomescapeException(() -> authService.signup(request), DomainErrorCode.INVALID_INPUT);
    }

    @DisplayName("이미 사용 중인 로그인 ID면 회원가입할 수 없다.")
    @Test
    void signupDuplicateLoginId() {
        SignupRequest request = new SignupRequest("새회원", "roro", "password", "password");
        given(memberDao.existsByLoginId("roro")).willReturn(true);

        assertRoomescapeException(() -> authService.signup(request), DomainErrorCode.DUPLICATE_MEMBER);
    }

    @DisplayName("회원가입 저장 중 유니크 제약이 발생해도 중복 회원 예외를 던진다.")
    @Test
    void signupDuplicateLoginIdRaceCondition() {
        SignupRequest request = new SignupRequest("새회원", "roro", "password", "password");
        given(memberDao.existsByLoginId("roro")).willReturn(false);
        given(memberDao.save(any(Member.class))).willThrow(new DuplicateKeyException("duplicate"));

        assertRoomescapeException(() -> authService.signup(request), DomainErrorCode.DUPLICATE_MEMBER);
    }

    private void assertRoomescapeException(Runnable runnable, DomainErrorCode code) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(code);
    }
}
