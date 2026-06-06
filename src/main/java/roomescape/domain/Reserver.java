package roomescape.domain;

import java.util.Objects;

public class Reserver {

    private final String name;

    public Reserver(String name) {
        validate(name);

        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reserver reserver)) return false;
        return Objects.equals(name, reserver.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    private void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("name은 255자를 넘을 수 없습니다.");
        }
    }
}
