package roomescape.feature.theme.domain;

import roomescape.global.domain.EntityStatus;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;
    private final EntityStatus status;

    private Theme(Long id, String name, String description, String imageUrl, EntityStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public static Theme create(String name, String description, String imageUrl) {
        return new Theme(null, name, description, imageUrl, EntityStatus.ACTIVE);
    }

    public static Theme reconstruct(Long id, String name, String description, String imageUrl, EntityStatus status) {
        return new Theme(id, name, description, imageUrl, status);
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

    public String getImageUrl() {
        return imageUrl;
    }

    public EntityStatus getStatus() {
        return status;
    }
}
