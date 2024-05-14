package roomescape.domain.member;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.exception.InvalidReservationException;

@Embeddable
public class MemberName {
    private static final int MINIMUM_NAME_LENGTH = 1;
    private static final int MAXIMUM_NAME_LENGTH = 20;
    private static final String INVALID_NAME_LENGTH = String.format("이름은 %d자 이상, %d자 이하여야 합니다.", MINIMUM_NAME_LENGTH,
            MAXIMUM_NAME_LENGTH);

    private String value;

    public MemberName() {
    }

    public MemberName(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String name) {
        if (name.length() < MINIMUM_NAME_LENGTH || name.length() > MAXIMUM_NAME_LENGTH) {
            throw new InvalidReservationException(INVALID_NAME_LENGTH);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MemberName memberName = (MemberName) o;
        return Objects.equals(value, memberName.value);
    }

    public String getValue() {
        return value;
    }
}
