package roomescape.domain.user;

public record Name(String name) {

    public Name {
        validate(name);
    }

    private void validate(final String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException(String.format("%s 는 유효하지 않은 값입니다.", name));
        }
    }

    public String asString() {
        return name;
    }
}
