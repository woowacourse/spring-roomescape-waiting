package roomescape.domain;

import roomescape.exception.InvalidDomainException;

public class Store {

    private final Long id;
    private final String name;

    public Store(Long id, String name) {
        validate(name);
        this.id = id;
        this.name = name;
    }

    private void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainException("매장 이름은 비어있을 수 없습니다.");
        }
    }

    public Store withId(Long id) {
        return new Store(id, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}