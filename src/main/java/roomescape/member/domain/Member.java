package roomescape.member.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Member {

    private final MemberId id;
    private final MemberName name;
    private final MemberEmail email;
    private final Role role;

    private static Member of(final MemberId id, final MemberName name, final MemberEmail email, final Role role) {
        validate(id, name, email, role);
        return new Member(id, name, email, role);
    }

    public static Member withId(final MemberId id, final MemberName name, final MemberEmail email, final Role role) {
        return of(id, name, email, role);
    }

    public static Member withoutId(final MemberName name, final MemberEmail email, final Role role) {
        return of(MemberId.unassigned(), name, email, role);
    }

    public static void validate(final MemberId id,
                                final MemberName name,
                                final MemberEmail email,
                                final Role role) {
        Validator.of(Member.class)
                .notNullField(Fields.id, id)
                .notNullField(Fields.name, name)
                .notNullField(Fields.email, email)
                .notNullField(Fields.role, role);
    }

    public boolean isAdmin() {
        return this.role.isEqual(Role.ADMIN);
    }
}
