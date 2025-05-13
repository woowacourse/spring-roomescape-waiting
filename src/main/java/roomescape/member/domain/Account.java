package roomescape.member.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.common.utils.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "member")
public class Account {

    private final Member member;
    private final Password password;

    public static Account of(final Member member, final Password password) {
        validate(member, password);
        return new Account(member, password);
    }

    public static void validate(final Member member, final Password password) {
        Validator.of(Account.class)
                .notNullField(Fields.member, member)
                .notNullField(Fields.password, password);
    }

    public boolean isSamePassword(final PasswordEncoder passwordEncoder, final String password) {
        return this.password.matches(passwordEncoder, password);
    }
}
