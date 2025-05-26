package roomescape.global.annotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import roomescape.domain.member.MemberRole;

@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckRole {
    MemberRole value();
}
