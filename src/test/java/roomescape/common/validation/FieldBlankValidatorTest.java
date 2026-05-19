package roomescape.common.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.common.validation.annotation.NotBlank;
import roomescape.common.validation.validator.FieldBlankValidator;

import java.util.List;

class FieldBlankValidatorTest {

    private final FieldBlankValidator validator = new FieldBlankValidator();

    @Test
    void 유효한_String값이_사용되면_검증예외가_발생하지않는다() {
        // given
        String validName = "송송";
        UseNotBlankDto validDto = new UseNotBlankDto(validName);

        // when
        List<String> errors = validator.validate(validDto);

        // then
        Assertions.assertThat(errors)
                .isEmpty();
    }

    @Test
    void NotBlank가_붙은_빌드가_비어있으면_예외가_발생한다() {
        // given
        String emptyName = " ";
        UseNotBlankDto emtpyNameDto = new UseNotBlankDto(emptyName);

        // when
        List<String> actual = validator.validate(emtpyNameDto);

        // then
        Assertions.assertThat(actual.getFirst())
                .isEqualTo("name은 비어있을 수 없습니다.");
    }

    @Test
    void NotBlank가_붙은_빌드가_Null이면_예외가_발생한다() {
        // given
        String nullName = null;
        UseNotBlankDto nullNameDto = new UseNotBlankDto(nullName);

        // when
        List<String> actual = validator.validate(nullNameDto);

        // then
        Assertions.assertThat(actual.getFirst())
                .isEqualTo("name은 Null일 수 없습니다.");
    }

    @Test
    void NotBlank가_붙은_빌드가_Strig타입이_아니면_예외가_발생한다() {
        // given
        WrongUseNotBlankDto wrongUseNotBlankDto = new WrongUseNotBlankDto(1L);

        // when
        List<String> actual = validator.validate(wrongUseNotBlankDto);

        // then
        Assertions.assertThat(actual.getFirst())
                .isEqualTo("@NotBlank는 오직 String 타입에만 사용할 수 있습니다.");
    }

    private record UseNotBlankDto(
            @NotBlank(message = "name은 비어있을 수 없습니다.")
            String name
    ) {
    }

    private record WrongUseNotBlankDto(
            @NotBlank
            Long id
    ) {
    }

}
