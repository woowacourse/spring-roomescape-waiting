package roomescape.member.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "member")
public class Account {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Member member;

    @Embedded
    private Password password;

    private static Account of(final Long id, final Member member, final Password password) {
        validate(member, password);
        return new Account(id, member, password);
    }

    public static Account withId(final Long id, final Member member, final Password password) {
        return of(id, member, password);
    }

    public static Account withoutId(final Member member, final Password password) {
        return of(null, member, password);
    }

    public static void validate(final Member member, final Password password) {
        Validator.of(Account.class)
                .notNullField(Fields.member, member)
                .notNullField(Fields.password, password);
    }

    public String getPasswordValue() {
        return password.getValue();
    }
}
