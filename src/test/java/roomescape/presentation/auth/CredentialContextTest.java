package roomescape.presentation.auth;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.application.auth.dto.TokenPayload;
import roomescape.domain.member.Role;
import roomescape.exception.UnAuthorizedException;

class CredentialContextTest {
    private CredentialContext context;

    @BeforeEach
    void setUp() {
        context = new CredentialContext();
    }

    @Test
    @DisplayName("값이 설정되지 않았을 때 가져오려는 경우 예외를 발생한다.")
    void exceptionOnNullMemberRole() {
        assertThatCode(context::getMemberId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("인증에 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("값이 설정되지 않았을 때, 권한을 설정하려는 경우 예외를 발생한다.")
    void exceptionOnPermissionCheckWithNullMemberRole() {
        assertThatCode(() -> context.validatePermission(Role.MEMBER))
                .isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    @DisplayName("값이 설정돼있을 때, 다시 설정하는 경우 예외를 발생한다.")
    void setCredentialOnAlreadyExistsTest() {
        context.setCredentialIfNotPresent(new TokenPayload(1L, "test", Role.MEMBER));
        assertThatCode(() -> context.setCredentialIfNotPresent(new TokenPayload(2L, "test", Role.MEMBER)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 인증 정보가 존재합니다.");
    }


    @Test
    @DisplayName("권한을 검증한다.")
    void validatePermissionTest() {
        context.setCredentialIfNotPresent(new TokenPayload(1L, "test", Role.MEMBER));
        assertThatCode(() -> context.validatePermission(Role.ADMIN))
                .isInstanceOf(UnAuthorizedException.class);
    }
}
