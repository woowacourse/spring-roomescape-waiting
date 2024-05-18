package roomescape.member.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.global.exception.ViolationException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Embeddable
@Access(AccessType.FIELD)
public class Name {
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\d+$");
    private static final int NAME_MAXIMUM_LENGTH = 10;

    @Column(nullable = false, name = "name")
    private String value;

    protected Name() {
    }

    public Name(String value) {
        validateLength(value);
        validatePattern(value);
        this.value = value;
    }

    private void validateLength(String name) {
        if (name == null || name.isBlank()) {
            throw new ViolationException("사용 이름은 비어있을 수 없습니다.");
        }
        if (name.length() > NAME_MAXIMUM_LENGTH) {
            throw new ViolationException("사용자 이름은 10자 이하입니다.");
        }
    }

    private void validatePattern(String name) {
        Matcher matcher = NAME_PATTERN.matcher(name);
        if (matcher.matches()) {
            throw new ViolationException("사용 이름은 숫자로만 구성될 수 없습니다.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name that = (Name) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
