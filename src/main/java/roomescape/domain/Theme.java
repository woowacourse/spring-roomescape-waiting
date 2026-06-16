package roomescape.domain;

import roomescape.domain.exception.DomainValidationException;

public class Theme {

    private static final int MAX_NAME_LENGTH = 30;

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;
    private final Long price;

    public Theme(Long id, String name, String description, String thumbnailUrl) {
        this(id, name, description, thumbnailUrl, 0L);
    }

    public Theme(Long id, String name, String description, String thumbnailUrl, Long price) {
        validateNameLength(name);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
    }

    private void validateNameLength(String name) {
        if (name.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException("테마 이름은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Long getPrice() {
        return price;
    }
}
