package roomescape.domain.theme;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.InvalidInputException;

class ThemeNameTest {

    @Test
    @DisplayName("이름에 공백이면 예외가 발생한다.")
    void nameCannotContainSpace() {
        assertThatThrownBy(() -> new ThemeName(" "))
            .isInstanceOf(InvalidInputException.class);
    }

    @Test
    @DisplayName("이름이 11글자 이상이면 예외가 발생한다.")
    void nameLengthCannotOverMax() {
        assertThatThrownBy(() -> new ThemeName("가".repeat(11)))
            .isInstanceOf(BusinessRuleViolationException.class);
    }
}
