package roomescape.common.validate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ValidatorTest {

    @Test
    @DisplayName("필드가 null이면 IllegalArgumentException이 발생한다")
    void validateValidateNotNullThrowsException() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when
        // then
        assertThatThrownBy(() -> validator.validateNotNull("sampleField", null, "예시 필드"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking null]: SampleClass.sampleField");
    }

    @Test
    @DisplayName("필드가 null이 아니면 예외가 발생하지 않는다")
    void validateValidateNotNullPasses() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when
        // then
        assertThat(validator.validateNotNull("sampleField", "", "예시 필드")).isNotNull();
    }

    @Test
    @DisplayName("blank 검사에서 필드가 null이거나 blank이면 예외가 발생한다")
    void validateValidateNotBlankThrowsException() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when
        // then
        assertThatThrownBy(() -> validator.validateNotBlank("sampleField", null, "예시 필드"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking blank]: SampleClass.sampleField");

        assertThatThrownBy(() -> validator.validateNotBlank("sampleField", " ", "예시 필드"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking blank]: SampleClass.sampleField");
    }

    @Test
    @DisplayName("필드가 blank가 아니면 예외가 발생하지 않는다")
    void validateValidateNotBlankPasses() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when
        // then
        assertThat(validator.validateNotBlank("sampleField", "content", "예시 필드")).isNotNull();
    }

    @Test
    @DisplayName("필드가 올바르지 않은 URI 형식이라면 예외가 발생한다")
    void validateUriFormat() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when
        // then
        assertThatThrownBy(() -> validator.validateUriFormat("sampleField", "ht^tp://invalidFormat.com", "예시 URI"))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking URI]: SampleClass.sampleField");
    }

    @Test
    @DisplayName("필드가 올바른 URI 형식이라면 예외가 발생하지 않는다")
    void validateUriFormatPasses() {
        // given
        final Validator validator = Validator.of(SampleClass.class);

        // when
        // then
        assertAll(() -> {
            assertThat(validator.validateNotBlank("sampleField", "validFormat.com", "예시 URI")).isNotNull();
            assertThat(validator.validateNotBlank("sampleField", "https://validFormat.com", "예시 URI")).isNotNull();
            assertThat(validator.validateNotBlank("sampleField", "validFormat", "예시 URI")).isNotNull();
        });
    }

    static class SampleClass {

        private String sampleField;
    }
}
