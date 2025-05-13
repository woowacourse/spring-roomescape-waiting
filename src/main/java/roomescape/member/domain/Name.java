package roomescape.member.domain;

import java.util.Objects;
import roomescape.member.exception.NameException;

public class Name {

    private final String name;

    public Name(String name) {
        validateNameIsNonEmpty(name);
        validateNameLength(name);

        this.name = name;
    }

    private void validateNameIsNonEmpty(final String name) {
        if (name == null || name.isEmpty()) {
            throw new NameException("이름은 비어있을 수 없습니다.");
        }
    }

    private void validateNameLength(String name) {
        if (name.isEmpty() || name.length() > 5) {
            throw new NameException("이름은 1-5글자 사이여야 합니다.");
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Name name1)) {
            return false;
        }
        return Objects.equals(getName(), name1.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }
}
