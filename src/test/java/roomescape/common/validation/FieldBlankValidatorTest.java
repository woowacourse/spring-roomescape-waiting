package roomescape.common.validation;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.common.validation.annotation.NotBlank;
import roomescape.common.validation.validator.FieldBlankValidator;

class FieldBlankValidatorTest {

    private final FieldBlankValidator validator = new FieldBlankValidator();

    private record UseNotBlankDto(@NotBlank(message = "name은 비어있을 수 없습니다.") String name) {

    }

    private record WrongUseNotBlankDto(@NotBlank Long id) {

    }

    @Nested
    @DisplayName("validate 메서드는")
    class ValidateTest {


        @Test
        @DisplayName("유효한 String값이 사용되면 검증예외가 발생하지않는다")
        void 성공() {
            // given
            String validName = "송송";
            UseNotBlankDto validDto = new UseNotBlankDto(validName);

            // when
            List<String> errors = validator.validate(validDto);

            // then
            Assertions.assertThat(errors).isEmpty();
        }
    }

    @Nested
    @DisplayName("validateNotBlank 메서드는")
    class ValidateNotBlankTest {


        @Test
        @DisplayName("NotBlank가 붙은 빌드가 비어있으면 예외가 발생한다")
        void 실패1() {
            // given
            String emptyName = " ";
            UseNotBlankDto emtpyNameDto = new UseNotBlankDto(emptyName);

            // when
            List<String> actual = validator.validate(emtpyNameDto);

            // then
            Assertions.assertThat(actual.getFirst()).isEqualTo("name은 비어있을 수 없습니다.");
        }


        @Test
        @DisplayName("NotBlank가 붙은 빌드가 Null이면 예외가 발생한다")
        void 실패2() {
            // given
            String nullName = null;
            UseNotBlankDto nullNameDto = new UseNotBlankDto(nullName);

            // when
            List<String> actual = validator.validate(nullNameDto);

            // then
            Assertions.assertThat(actual.getFirst()).isEqualTo("name은 Null일 수 없습니다.");
        }


        @Test
        @DisplayName("NotBlank가 붙은 빌드가 String 타입이 아니면 예외가 발생한다")
        void 실패3() {
            // given
            WrongUseNotBlankDto wrongUseNotBlankDto = new WrongUseNotBlankDto(1L);

            // when
            List<String> actual = validator.validate(wrongUseNotBlankDto);

            // then
            Assertions.assertThat(actual.getFirst())
                .isEqualTo("@NotBlank는 오직 String 타입에만 사용할 수 있습니다.");
        }
    }
}
