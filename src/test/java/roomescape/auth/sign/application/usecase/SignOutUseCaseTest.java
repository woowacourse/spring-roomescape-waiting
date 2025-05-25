package roomescape.auth.sign.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.sign.application.dto.SignInResult;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SignOutUseCaseTest {

    @Autowired
    private SignOutUseCase signOutUseCase;

    @Test
    @DisplayName("로그아웃은 쿠키에 세팅할 수 있는 만료된 토큰을 값을 반환한다.")
    public void testSignOutUseCase() {
        // when
        final SignInResult result = signOutUseCase.execute();

        // then
        assertThat(result.cookie().getName()).isEqualTo(TokenType.ACCESS.getDescription());
        assertThat(result.cookie().getMaxAge() <= 0).isTrue();
    }
}
