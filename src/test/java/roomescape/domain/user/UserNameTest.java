package roomescape.domain.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.InvalidInputException;

class UserNameTest {

    @Test
    @DisplayName("이름에 공백이 포함되면 예외가 발생한다.")
    void userNameCannotContainSpace() {
        assertThatThrownBy(() -> new UserName("공백 이름"))
            .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("이름이 6글자 이상이면 예외가 발생한다.")
    void nameLengthCannotOverMax() {
        assertThatThrownBy(() -> new UserName("여섯글자이름"))
            .isInstanceOf(BusinessRuleViolationException.class);
    }
}
