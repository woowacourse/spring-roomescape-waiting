package roomescape.domain.theme.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ThemeCreateRequestDtoTest {

    private final Validator validator = Validation
        .buildDefaultValidatorFactory()
        .getValidator();

    @Nested
    class Success {

        @Test
        void 테마_생성_요청_Dto가_유효하면_검증_오류가_없다() {
            // given
            ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                "테마 이름",
                "테마 설명",
                "https://roomescape.com/images/themes/theme.png"
            );

            // when
            Set<ConstraintViolation<ThemeCreateRequestDto>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    class Failed {

        @Test
        void name이_공백이면_검증_오류가_발생한다() {
            // given
            ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                " ",
                "테마 설명",
                "https://roomescape.com/images/themes/theme.png"
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("테마 이름은 필수입니다.");
        }

        @Test
        void name의_길이가_255_초과이면_검증_오류가_발생한다() {
            // given
            ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                "a".repeat(256),
                "테마 설명",
                "https://roomescape.com/images/themes/theme.png"
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("테마 이름의 길이는 255 이하여야 합니다.");
        }

        @Test
        void description이_공백이면_검증_오류가_발생한다() {
            // given
            ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                "테마 이름",
                " ",
                "https://roomescape.com/images/themes/theme.png"
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("테마 설명은 필수입니다.");
        }

        @Test
        void description의_길이가_255_초과이면_검증_오류가_발생한다() {
            // given
            ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                "테마 이름",
                "a".repeat(256),
                "https://roomescape.com/images/themes/theme.png"
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("테마 설명의 길이는 255 이하여야 합니다.");
        }

        @Test
        void imageUrl이_공백이면_검증_오류가_발생한다() {
            // given
            ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                "테마 이름",
                "테마 설명",
                " "
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("테마 이미지 URL은 필수입니다.");
        }

        @Test
        void imageUrl의_길이가_2000_초과이면_검증_오류가_발생한다() {
            // given
            ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                "테마 이름",
                "테마 설명",
                "https://roomescape.com/" + "a".repeat(2000)
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("테마 이미지 URL의 길이는 2000 이하여야 합니다.");
        }

        @Test
        void imageUrl의_URL_형식이_올바르지_않으면_검증_오류가_발생한다() {
            // given
            ThemeCreateRequestDto request = new ThemeCreateRequestDto(
                "테마 이름",
                "테마 설명",
                "invalid-url"
            );

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("테마 이미지 URL 형식이 올바르지 않습니다.");
        }
    }

    private Set<String> validate(ThemeCreateRequestDto request) {
        Set<ConstraintViolation<ThemeCreateRequestDto>> violations = validator.validate(request);

        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(java.util.stream.Collectors.toSet());
    }
}
