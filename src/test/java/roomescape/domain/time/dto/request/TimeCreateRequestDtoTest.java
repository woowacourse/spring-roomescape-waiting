package roomescape.domain.time.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TimeCreateRequestDtoTest {

    private final Validator validator = Validation
        .buildDefaultValidatorFactory()
        .getValidator();

    @Nested
    class Success {

        @Test
        void 예약_시간_생성_요청_Dto가_유효하면_검증_오류가_없다() {
            // given
            TimeCreateRequestDto request = new TimeCreateRequestDto(LocalTime.of(20, 30));

            // when
            Set<ConstraintViolation<TimeCreateRequestDto>> violations = validator.validate(request);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    class Failed {

        @Test
        void 예약_시간이_null이면_검증_오류가_발생한다() {
            // given
            TimeCreateRequestDto request = new TimeCreateRequestDto(null);

            // when
            Set<String> violationMessages = validate(request);

            // then
            assertThat(violationMessages).contains("예약 시간은 필수입니다.");
        }
    }

    private Set<String> validate(TimeCreateRequestDto request) {
        Set<ConstraintViolation<TimeCreateRequestDto>> violations = validator.validate(request);

        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(java.util.stream.Collectors.toSet());
    }
}
