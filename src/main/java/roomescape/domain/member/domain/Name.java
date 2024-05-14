package roomescape.domain.member.domain;

import java.util.Objects;
import roomescape.global.exception.EscapeApplicationException;

public class Name {

    private final String name;

    public Name(String name) {
        validateNonBlank(name);
        this.name = name;
    }

    private void validateNonBlank(String name) {
        if (name == null) {
            throw new EscapeApplicationException("이름은 비어있을 수 없습니다.");
        }
        if (name.trim().isEmpty()) {
            throw new EscapeApplicationException("이름의 길이는 공백을 제외한 1이상이어야합니다.");
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Name name1 = (Name) o;
        return Objects.equals(name, name1.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
