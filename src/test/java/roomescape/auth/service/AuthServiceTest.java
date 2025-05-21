package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.TokenResponse;
import roomescape.fake.FakeMemberDao;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.auth.LoginMember;
import roomescape.global.exception.custom.ForbiddenException;
import roomescape.global.exception.custom.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;

class AuthServiceTest {

    private final FakeMemberDao fakeMemberDao = new FakeMemberDao();
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    private final AuthService authService = new AuthService(fakeMemberDao, jwtTokenProvider);

    private final static long USER_ID = 1L;
    private final static MemberName USER_NAME = new MemberName("사용자");
    private final static MemberEmail USER_EMAIL = new MemberEmail("aaa@gmail.com");
    private final static String USER_PASSWORD = "1234";

    @BeforeEach
    void setUp() {
        fakeMemberDao.save(new Member(USER_ID, USER_NAME, USER_EMAIL, USER_PASSWORD, Role.USER));
    }


    @DisplayName("토큰 생성 테스트")
    @Nested
    class CreateTokenTest {

        @DisplayName("사용자 정보를 찾아서 토큰을 반환할 수 있다.")
        @Test
        void testCreateToken() {
            // when
            TokenResponse token = authService.createToken(new LoginRequest(USER_EMAIL.getValue(), USER_PASSWORD));
            // then
            long id = jwtTokenProvider.getId(token.accessToken());
            assertThat(Long.valueOf(id)).isEqualTo(USER_ID);
        }

        @DisplayName("이메일이 일치하지 않을 경우 예외가 발생한다.")
        @Test
        void testInvalidEmail() {
            // given
            LoginRequest request = new LoginRequest("bbbb@email.com", USER_PASSWORD);
            // when
            // then
            assertThatThrownBy(() -> authService.createToken(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("올바르지 않은 로그인 정보입니다.");
        }

        @DisplayName("비밀번호가 일치하지 않을 경우 예외가 발생한다.")
        @Test
        void testInvalidPassword() {
            // given
            LoginRequest request = new LoginRequest(USER_EMAIL.getValue(), "4321");
            // when
            // then
            assertThatThrownBy(() -> authService.createToken(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("올바르지 않은 로그인 정보입니다.");
        }
    }

    @DisplayName("인증 정보 조회 테스트")
    @Nested
    class checkMemberTest {

        @DisplayName("토큰의 ID를 추출하여 해당하는 사용자 정보를 반환한다.")
        @Test
        void testCheckMember() {
            // given
            String token = jwtTokenProvider.createToken(String.valueOf(USER_ID));
            // when
            LoginMember response = authService.checkMember(token);
            // then
            assertThat(response.name()).isEqualTo(USER_NAME.getValue());
        }

        @DisplayName("올바르지 않은 ID일 경우 예외가 발생한다.")
        @Test
        void testInvalidId() {
            // given
            String invalidToken = jwtTokenProvider.createToken(String.valueOf(3L));
            // when
            // then
            assertThatThrownBy(() -> authService.checkMember(invalidToken))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("확인할 수 없는 사용자입니다.");
        }
    }

    @DisplayName("관리자 인증 정보 조회 테스트")
    @Nested
    class checkAdminMemberTest {

        private final static long ADMIN_ID = 2L;
        private final static MemberName ADMIN_NAME = new MemberName("관리자");
        private final static MemberEmail ADMIN_EMAIL = new MemberEmail("admin@gmail.com");
        private final static String ADMIN_PASSWORD = "1234";

        @BeforeEach
        void setUp() {
            fakeMemberDao.save(new Member(ADMIN_ID, ADMIN_NAME, ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN));
        }

        @DisplayName("토큰의 ID를 추출하여 해당하는 관리자 정보를 반환한다.")
        @Test
        void testCheckAdminMember() {
            // given
            String token = jwtTokenProvider.createToken(String.valueOf(ADMIN_ID));
            // when
            LoginMember response = authService.checkAdminMember(token);
            // then
            assertThat(response.name()).isEqualTo(ADMIN_NAME.getValue());
        }

        @DisplayName("올바르지 않은 ID일 경우 예외가 발생한다.")
        @Test
        void testInvalidId() {
            // given
            String invalidToken = jwtTokenProvider.createToken(String.valueOf(3L));
            // when
            // then
            assertThatThrownBy(() -> authService.checkAdminMember(invalidToken))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("확인할 수 없는 사용자입니다.");
        }

        @DisplayName("관리자 권한이 없는 경우 예외가 발생한다.")
        @Test
        void testInvalidIdRole() {
            // given
            String invalidToken = jwtTokenProvider.createToken(String.valueOf(USER_ID));
            // when
            // then
            assertThatThrownBy(() -> authService.checkAdminMember(invalidToken))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("접근 권한이 없습니다.");
        }
    }
}
