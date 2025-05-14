package roomescape.member.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.common.utils.Validator;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "member")
public class Account {

    @EmbeddedId
    private AccountId id;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Embedded
    private Password password;

    public static Account of(final Member member, final Password password) {
        validate(member, password);
        return new Account(null, member, password);
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
