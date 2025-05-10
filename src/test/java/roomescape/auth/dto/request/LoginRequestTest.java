package roomescape.auth.dto.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidInputException;

class LoginRequestTest {

    @Test
    void 이메일에_빈_값이_들어올_수_없다() {
        // Given
        // When
        // Then
        Assertions.assertThatThrownBy(() -> new LoginRequest(null, "password"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("이메일이 입력되지 않았다.");
    }

    @Test
    void 패스워드에_빈_값이_들어올_수_없다() {
        // Given
        // When
        // Then
        Assertions.assertThatThrownBy(() -> new LoginRequest("email@email.com", null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("패스워드가 입력되지 않았다.");
    }
}