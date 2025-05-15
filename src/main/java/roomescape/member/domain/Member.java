package roomescape.member.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private MemberName name;

    @Embedded
    private MemberEmail email;

    @Enumerated(EnumType.STRING)
    private Role role;

    private static Member of(final Long id, final MemberName name, final MemberEmail email, final Role role) {
        validate(name, email, role);
        return new Member(id, name, email, role);
    }

    public static Member withId(final Long id, final MemberName name, final MemberEmail email, final Role role) {
        return of(id, name, email, role);
    }

    public static Member withoutId(final MemberName name, final MemberEmail email, final Role role) {
        return of(null, name, email, role);
    }

    public static void validate(final MemberName name,
                                final MemberEmail email,
                                final Role role) {
        Validator.of(Member.class)
                .notNullField(Fields.name, name)
                .notNullField(Fields.email, email)
                .notNullField(Fields.role, role);
    }

    public boolean isAdmin() {
        return this.role.isEqual(Role.ADMIN);
    }
}
