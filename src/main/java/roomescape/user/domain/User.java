package roomescape.user.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.DomainTerm;
import roomescape.common.domain.Email;
import roomescape.common.validate.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PUBLIC)
@EqualsAndHashCode(of = "id")
@ToString
public class User {

    private final UserId id;
    private final UserName name;
    private final Email email;
    private final Password password;
    private final UserRole role;

    private static User of(final UserId id,
                           final UserName name,
                           final Email email,
                           final Password password,
                           final UserRole role) {
        validate(id, name, email, password, role);
        return new User(id, name, email, password, role);
    }

    public static User withId(final UserId id,
                              final UserName name,
                              final Email email,
                              final Password password,
                              final UserRole role) {
        id.requireAssigned();
        return of(id, name, email, password, role);
    }

    public static User withoutId(final UserName name,
                                 final Email email,
                                 final Password password,
                                 final UserRole role) {
        return of(UserId.unassigned(), name, email, password, role);
    }

    private static void validate(final UserId id,
                                 final UserName name,
                                 final Email email,
                                 final Password password,
                                 final UserRole role) {
        Validator.of(User.class)
                .validateNotNull(Fields.id, id, DomainTerm.USER_ID.label())
                .validateNotNull(Fields.name, name, DomainTerm.USER_NAME.label())
                .validateNotNull(Fields.email, email, DomainTerm.USER_EMAIL.label())
                .validateNotNull(Fields.password, password, DomainTerm.USER_PASSWORD.label())
                .validateNotNull(Fields.role, role, DomainTerm.USER_ROLE.label());
    }
}
