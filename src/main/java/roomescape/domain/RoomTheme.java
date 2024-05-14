package roomescape.domain;

import roomescape.exception.BadRequestException;

public class RoomTheme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public RoomTheme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public RoomTheme(Long id, String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new BadRequestException("썸네일에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BadRequestException("설명에 빈값을 입력할 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("이름에 빈값을 입력할 수 없습니다.");
        }
    }

    public RoomTheme withId(Long id) {
        return new RoomTheme(id, name, description, thumbnail);
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

    public String getThumbnail() {
        return thumbnail;
    }
}
