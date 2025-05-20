package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Password {

    @Column(name = "password", nullable = false)
    private String value;

    public static Password createForLoginMember() {
        return new Password(null);
    }

    public static Password createForMember(@NonNull final String password) {
        return new Password(password);
    }

    public boolean matchesPassword(final String password) {
        return value.equals(password);
    }
}
