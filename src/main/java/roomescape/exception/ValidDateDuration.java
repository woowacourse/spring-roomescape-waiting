package roomescape.exception;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DateDurationValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateDuration {

    String message() default "최대 {days}일 까지만 조회가능합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int days() default 30;
}
