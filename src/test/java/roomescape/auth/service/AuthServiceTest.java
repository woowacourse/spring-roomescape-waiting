package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.TokenResponse;
import roomescape.fake.FakeMemberDao;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.exception.custom.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;

class AuthServiceTest {

    private final FakeMemberDao fakeMemberDao = new FakeMemberDao();
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    private final AuthService authService = new AuthService(fakeMemberDao, jwtTokenProvider);

    private final static long SAVED_ID = 1L;
    private final static MemberName SAVED_NAME = new MemberName("사용자");
    private final static MemberEmail SAVED_EMAIL = new MemberEmail("aaa@gmail.com");
    private final static String SAVED_PASSWORD = "1234";

    @BeforeEach
    void setUp() {
        fakeMemberDao.save(new Member(SAVED_ID, SAVED_NAME, SAVED_EMAIL, SAVED_PASSWORD, Role.USER));
    }


    @DisplayName("토큰 생성 테스트")
    @Nested
    class CreateTokenTest {

        @DisplayName("사용자 정보를 찾아서 토큰을 반환할 수 있다.")
        @Test
        void testCreateToken() {
            // when
            TokenResponse token = authService.createToken(new LoginRequest(SAVED_EMAIL.getEmail(), SAVED_PASSWORD));
            // then
            long id = jwtTokenProvider.getId(token.accessToken());
            assertThat(Long.valueOf(id)).isEqualTo(SAVED_ID);
        }

        @DisplayName("이메일이 일치하지 않을 경우 예외가 발생한다.")
        @Test
        void testInvalidEmail() {
            // given
            LoginRequest request = new LoginRequest("bbbb@email.com", SAVED_PASSWORD);
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
            LoginRequest request = new LoginRequest(SAVED_EMAIL.getEmail(), "4321");
            // when
            // then
            assertThatThrownBy(() -> authService.createToken(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("올바르지 않은 로그인 정보입니다.");
        }
    }

    @DisplayName("인증 정보 조회 테스트")
    @Nested
    class checkMember {

        @DisplayName("토큰의 ID를 추출하여 해당하는 사용자 이름을 반환한다.")
        @Test
        void testCheckMember() {
            // given
            String token = jwtTokenProvider.createToken(String.valueOf(SAVED_ID));
            // when
            LoginCheckResponse response = authService.checkMember(token);
            // then
            assertThat(response.name()).isEqualTo(SAVED_NAME.getName());
        }

        @DisplayName("올바르지 않은 ID일 경우 예외가 발생한다.")
        @Test
        void testInvalidId() {
            // given
            String invalidToken = jwtTokenProvider.createToken(String.valueOf(2L));
            // when
            // then
            assertThatThrownBy(() -> authService.checkMember(invalidToken))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("확인할 수 없는 사용자입니다.");
        }
    }
}
