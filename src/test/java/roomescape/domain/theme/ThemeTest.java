package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessRuleViolationException;

class ThemeTest {

    @Test
    @DisplayName("이름이 10자를 초과하는 경우 예외를 던진다.")
    void validateName_WhenTooLong() {
        // given
        var tooLongName = "이름이10자를초과하는경우";
        var description = "우테코 레벨1을 탈출하는 내용입니다.";
        var thumbnail = "https://image.url";

        // when & then
        assertThatThrownBy(() -> Theme.register(tooLongName, description, thumbnail))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("이름은 10자를 넘길 수 없습니다.");
    }

    @Test
    @DisplayName("설명이 50자를 초과하는 경우 예외를 던진다.")
    void validateDescription_WhenTooLong() {
        // given
        var name = "레벨1 탈출";
        var tooLongDescription = "a".repeat(51);
        var thumbnail = "https://image.url";

        // when & then
        assertThatThrownBy(() -> Theme.register(name, tooLongDescription, thumbnail))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("설명은 50자를 넘길 수 없습니다.");
    }
}
