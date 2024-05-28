package roomescape.service.dto.validator;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Positive;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// id 메시지 공통화를 위해 커스텀 어노테이션 정의
@Positive(message = "1 이상의 값만 입력해주세요.")
@Constraint(validatedBy = {})
@Documented
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface IdPositive {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
