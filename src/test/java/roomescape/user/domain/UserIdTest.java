package roomescape.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserIdTest {

    @Test
    @DisplayName("사용자 ID가 null이면 예외가 발생한다")
    void validateNullUserId() {
        // when
        // then
        assertThatThrownBy(() -> UserId.from(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking null]: DomainId.value");
    }

    @Test
    @DisplayName("유효한 ID로 UserId 객체를 생성할 수 있다")
    void createValidUserId() {
        // given
        final Long id = 1L;

        // when
        final UserId userId = UserId.from(id);

        // then
        assertAll(() -> {
            assertThat(userId).isNotNull();
            assertThat(userId.getValue()).isEqualTo(id);
        });
    }
} 