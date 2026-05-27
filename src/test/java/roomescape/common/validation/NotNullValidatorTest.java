package roomescape.common.validation;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.common.validation.annotation.NotNull;
import roomescape.common.validation.validator.NotNullValidator;

class NotNullValidatorTest {

    private final NotNullValidator notNullValidator = new NotNullValidator();

    private record UseNotNullDto(
        @NotNull(message = "id는 Null일 수 없습니다.")
        Long id
    ) {

    }

    @Nested
    @DisplayName("validate 메서드는")
    class ValidateTest {

        @Test
        @DisplayName("Null이 아닌값을 사용하면 검증 예외가 발생하지않는다")
        void 성공() {
            // given
            Long validId = 1L;
            UseNotNullDto validDto = new UseNotNullDto(validId);

            // when
            List<String> errors = notNullValidator.validate(validDto);

            // then
            Assertions.assertThat(errors)
                .isEmpty();
        }
    }

    @Nested
    @DisplayName("validateNotNull 메서드는")
    class ValidateNotNullTest {

        @Test
        @DisplayName("NotNull이 붙은 필드가 Null이면 예외가 발생한다")
        void 성공() {
            // given
            Long nullId = null;
            UseNotNullDto useNotNullDto = new UseNotNullDto(nullId);

            // when
            List<String> actual = notNullValidator.validate(useNotNullDto);

            // then
            Assertions.assertThat(actual.getFirst())
                .isEqualTo("id는 Null일 수 없습니다.");
        }
    }
}
