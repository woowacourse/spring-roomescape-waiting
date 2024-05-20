package roomescape.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DateRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {

    String message() default "최대 " + DateRangeValidator.LIMIT_DAY + "일 까지만 조회가능합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
