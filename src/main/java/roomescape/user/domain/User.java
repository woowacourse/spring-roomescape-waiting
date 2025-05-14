package roomescape.user.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.BaseEntity;
import roomescape.common.domain.DomainTerm;
import roomescape.common.domain.Email;
import roomescape.common.validate.Validator;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldNameConstants
@ToString
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Embedded
    @AttributeOverride(
            name = UserName.Fields.value,
            column = @Column(name = Fields.name)
    )
    private UserName name;

    @Embedded
    @AttributeOverride(
            name = Email.Fields.value,
            column = @Column(name = Fields.email)
    )
    private Email email;

    @Embedded
    @AttributeOverride(
            name = Password.Fields.encodedValue,
            column = @Column(name = Fields.password)
    )
    private Password password;

    @Enumerated(value = EnumType.STRING)
    @Column(name = Fields.role)
    private UserRole role;

    public User(final UserName name, final Email email, final Password password, final UserRole role) {
        validate(name, email, password, role);
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(final Long id, final UserName name, final Email email, final Password password, final UserRole role) {
        super(id);
        validate(name, email, password, role);
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static User withId(final UserId id,
                              final UserName name,
                              final Email email,
                              final Password password,
                              final UserRole role) {
        id.requireAssigned();
        return new User(id.getValue(), name, email, password, role);
    }

    public static User withoutId(final UserName name,
                                 final Email email,
                                 final Password password,
                                 final UserRole role) {
        return new User(name, email, password, role);
    }

    private static void validate(final UserName name,
                                 final Email email,
                                 final Password password,
                                 final UserRole role) {
        Validator.of(User.class)
                .validateNotNull(Fields.name, name, DomainTerm.USER_NAME.label())
                .validateNotNull(Fields.email, email, DomainTerm.USER_EMAIL.label())
                .validateNotNull(Fields.password, password, DomainTerm.USER_PASSWORD.label())
                .validateNotNull(Fields.role, role, DomainTerm.USER_ROLE.label());
    }

    public UserId getId() {
        return UserId.from(id);
    }
}
