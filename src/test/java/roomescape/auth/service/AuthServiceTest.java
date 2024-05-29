package roomescape.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.provider.FakeTokenProvider;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;
import roomescape.member.repository.FakeMemberRepository;

class AuthServiceTest {

    private final AuthService authService;

    public AuthServiceTest() {
        this.authService = new AuthService(new FakeMemberRepository(), new FakeTokenProvider());
    }

    @Test
    @DisplayName("가입하지 않은 유저가 로그인 하는 경우 에러가 발생한다.")
    void notExistMemberLogin() {
        LoginRequest loginRequest = new LoginRequest("pollari@gmail.com", "polla99");

        Throwable notExistMember = assertThrows(RoomEscapeException.class,
                () -> authService.login(loginRequest));

        assertEquals(notExistMember.getMessage(), MemberExceptionCode.ID_AND_PASSWORD_NOT_MATCH_OR_EXIST.getMessage());
    }
}
