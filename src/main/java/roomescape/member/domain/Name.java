package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

@Embeddable
public class Name {
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z가-힣0-9]+(?:\\s+[a-zA-Z가-힣0-9]+)*$");
    private static final int MAX_NAME_LENGTH = 10;

    private String name;

    public Name(String name) {
        validate(name);
        this.name = name;
    }

    public Name() {
    }

    private void validate(String name) {
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH || !PATTERN.matcher(name).matches()) {
            throw new BusinessException(ErrorType.NAME_FORMAT_ERROR);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Name name1 = (Name) o;
        return Objects.equals(getName(), name1.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return "Name{" +
                "name='" + name + '\'' +
                '}';
    }
}
