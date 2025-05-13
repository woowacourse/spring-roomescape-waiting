package roomescape.member.domain;

import java.util.Objects;

public final class MemberName {

    private final String name;

    public MemberName(final String name) {
        validateName(name);
        this.name = name;
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank() || name.length() > 5) {
            throw new IllegalArgumentException("사용자명은 최소 1글자, 최대 5글자여야합니다.");
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

        final MemberName that = (MemberName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
