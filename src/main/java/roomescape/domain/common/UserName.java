package roomescape.domain.common;

import java.util.Objects;

public class UserName {
    private final String name;

    private UserName(String name) {
        this.name = name;
    }

    public static UserName from(String name) {
        Objects.requireNonNull(name, "이름은 필수입니다.");

        return new UserName(name.trim());
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserName userName = (UserName) o;
        return Objects.equals(name, userName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
