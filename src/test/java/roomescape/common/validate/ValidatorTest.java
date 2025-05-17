package roomescape.common.validate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidInputException;
import roomescape.common.utils.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatorTest {

    @Test
    @DisplayName("필드가 null이면 InvalidInputException이 발생한다")
    void validateNotNullFieldThrowsException() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when & then
        assertThatThrownBy(() -> validator.notNullField("sampleField", null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("SampleClass.sampleField 은(는) null일 수 없습니다.");
    }

    @Test
    @DisplayName("필드가 null이 아니면 예외가 발생하지 않는다")
    void validateNotNullFieldPasses() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when & then
        assertThat(validator.notNullField("sampleField", "")).isNotNull();
    }

    @Test
    @DisplayName("필드가 null이거나 blank이면 InvalidInputException이 발생한다")
    void validateNotBlankFieldThrowsException() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when & then
        assertThatThrownBy(() -> validator.notBlankField("sampleField", null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("SampleClass.sampleField 은(는) 비어있을 수 없습니다.");

        assertThatThrownBy(() -> validator.notBlankField("sampleField", " "))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("SampleClass.sampleField 은(는) 비어있을 수 없습니다.");
    }

    @Test
    @DisplayName("필드가 blank가 아니면 예외가 발생하지 않는다")
    void validateNotBlankFieldPasses() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when & then
        assertThat(validator.notBlankField("sampleField", "content")).isNotNull();
    }

    @Test
    @DisplayName("이메일 필드가 이메일 형식이 아니라면 예외가 발생한다")
    void validateEmailField() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when & then
        assertThatThrownBy(() -> validator.emailField("email", "siso.com"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("SampleClass.email 은(는) 유효한 이메일 형식이 아닙니다.");
    }

    @Test
    @DisplayName("이메일 필드가 이메일 형식이면 예외가 발생하지 않는다")
    void validateEmailFieldPass() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when & then
        assertThat(validator.emailField("email", "siso@naver.com")).isNotNull();
    }

    static class SampleClass {

        private String sampleField;
    }
}
