package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.system.exception.RoomescapeException;

@Embeddable
public class Name {

    private static final int MAX_NAME_LENGTH = 20;

    @Column(name = "name")
    private String value;

    public Name(String value) {
        validate(value);
        this.value = value;
    }

    protected Name() {
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new RoomescapeException("예약자 이름은 null이거나 비어 있을 수 없습니다.");
        }
        if (value.length() > MAX_NAME_LENGTH) {
            throw new RoomescapeException(String.format("예약자 이름은 최대 %d글자입니다.", MAX_NAME_LENGTH));
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Name name = (Name) o;
        return Objects.equals(value, name.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
