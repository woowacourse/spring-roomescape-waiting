package roomescape.domain.theme;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessRuleViolationException;

class DescriptionTest {

    @Test
    @DisplayName("설명이 50자 초과이면 예외가 발생한다.")
    void descriptionLengthCannotOverMax() {
        assertThatThrownBy(() -> new Description(
            "가".repeat(51)
        )).isInstanceOf(BusinessRuleViolationException.class);
    }
}
