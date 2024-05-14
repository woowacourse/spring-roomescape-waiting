package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.WRONG_PASSWORD;

import io.jsonwebtoken.Claims;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.Fixture;
import roomescape.domain.LoginMember;
import roomescape.domain.Member;
import roomescape.dto.LoginRequest;
import roomescape.exception.RoomescapeException;
import roomescape.repository.CollectionMemberRepository;
import roomescape.repository.MemberRepository;

class LoginServiceTest {

    private static final JwtGenerator JWT_GENERATOR = new JwtGenerator("secretKey", 360000);
    private MemberRepository memberRepository;
    private LoginService loginService;

    @BeforeEach
    void init() {
        memberRepository = new CollectionMemberRepository();
        loginService = new LoginService(memberRepository, JWT_GENERATOR);
    }

    @DisplayName("유저 데이터가 존재할 때")
    @Nested
    class MemberExistsTest {
        private Member defaultUser = Fixture.defaultMember;

        @BeforeEach
        void addDefaultUser() {
            defaultUser = memberRepository.save(defaultUser);
        }

        @DisplayName("정상적인 로그인에 대해 토큰을 생성할 수 있다.")
        @Test
        void createTokenTest() {
            //when
            String createdToken = loginService.getLoginToken(new LoginRequest(
                    defaultUser.getEmail(),
                    defaultUser.getPassword()
            ));

            //then
            Claims payload = JWT_GENERATOR.getClaims(createdToken);
            assertAll(
                    () -> assertThat(payload.get("id", Long.class)).isEqualTo(defaultUser.getId()),
                    () -> assertThat(payload.get("name")).isEqualTo(defaultUser.getName())
            );
        }

        @DisplayName("존재하지 않는 email 요청을 하면 예외가 발생한다.")
        @Test
        void notFoundEmailGetTokenTest() {
            assertThatThrownBy(() -> loginService.getLoginToken(new LoginRequest(
                    defaultUser.getEmail() + "wrong",
                    defaultUser.getPassword()
            )))
                    .isInstanceOf(RoomescapeException.class)
                    .hasMessage(NOT_FOUND_MEMBER.getMessage());
        }

        @DisplayName("잘못된 비밀번호로 요청을 하면 예외가 발생한다.")
        @Test
        void illegalPasswordGetTokenTest() {
            assertThatThrownBy(() -> loginService.getLoginToken(new LoginRequest(
                    defaultUser.getEmail(),
                    defaultUser.getPassword() + " wrong"
            )))
                    .isInstanceOf(RoomescapeException.class)
                    .hasMessage(WRONG_PASSWORD.getMessage());
        }

        @DisplayName("로그인이 되었을 때 토큰으로 사용자 이름을 찾을 수 있다.")
        @Test
        void loginTokenContainsUserNameTest() {
            //given
            String token = JWT_GENERATOR.generateWith(Map.of(
                    "id", defaultUser.getId(),
                    "name", defaultUser.getName(),
                    "role", defaultUser.getRole().getTokenValue()
            ));

            //when
            LoginMember loginMember = loginService.checkLogin(token);

            //then
            assertThat(loginMember.getName())
                    .isEqualTo(defaultUser.getName());
        }
    }
}
