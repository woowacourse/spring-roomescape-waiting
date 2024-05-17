package roomescape.presentation.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import roomescape.domain.role.Role;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionRequired {

    Role value() default Role.ADMIN;

    @AliasFor("value")
    Role role() default Role.ADMIN;
}
