package roomescape.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserNameTest {

    @Test
    @DisplayName("사용자 이름이 null이면 예외가 발생한다")
    void validateNullUserName() {
        // when
        // then
        assertThatThrownBy(() -> UserName.from(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking blank]: UserName.value");
    }

    @Test
    @DisplayName("사용자 이름이 빈 문자열이면 예외가 발생한다")
    void validateBlankUserName() {
        // when
        // then
        assertAll(() -> {
            assertThatThrownBy(() -> UserName.from(""))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: UserName.value");

            assertThatThrownBy(() -> UserName.from(" "))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: UserName.value");
        });
    }

    @Test
    @DisplayName("유효한 사용자 이름으로 UserName 객체를 생성할 수 있다")
    void createValidUserName() {
        // when
        final UserName userName = UserName.from("홍길동");

        // then
        assertAll(() -> {
            assertThat(userName).isNotNull();
            assertThat(userName.getValue()).isEqualTo("홍길동");
        });
    }
}
