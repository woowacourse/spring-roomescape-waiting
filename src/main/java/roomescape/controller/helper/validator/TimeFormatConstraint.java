package roomescape.controller.helper.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {TimeFormatValidator.class}
)
public @interface TimeFormatConstraint {

    String message() default "시간 입력 형식이 올바르지 않습니다. ex) 13:40";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
