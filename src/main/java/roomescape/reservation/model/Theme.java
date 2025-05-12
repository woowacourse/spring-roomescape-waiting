package roomescape.reservation.model;

import roomescape.global.exception.AlreadyEntityException;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public Theme(Long id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme toEntity(Long id) {
        if (this.id == null) {
            return new Theme(id, name, description, thumbnail);
        }
        throw new AlreadyEntityException("해당 테마는 이미 엔티티화 된 상태입니다.");
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
