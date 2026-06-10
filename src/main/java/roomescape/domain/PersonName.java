package roomescape.domain;

public record PersonName(
        String name
) {

    public PersonName {
        validate(name);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이름은 비워둘 수 없습니다.");
        }
    }

    public boolean isSameName(final String name) {
        return this.name.equals(name);
    }
}
