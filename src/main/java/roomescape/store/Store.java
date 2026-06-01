package roomescape.store;

public class Store {

    private static final int MAX_NAME_LENGTH = 50;

    private final Long id;
    private final String name;

    public Store(Long id, String name) {
        validateName(name);

        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("매장 이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("매장 이름은 " + MAX_NAME_LENGTH + "자 이하여야 합니다.");
        }
    }
}
