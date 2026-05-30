package roomescape.feature.theme.domain;

import roomescape.global.domain.EntityStatus;

public class Theme {

    private final Long id;
    private final ThemeName name;
    private final ThemeDescription description;
    private final ThemeImageUrl imageUrl;
    private final EntityStatus status;

    private Theme(Long id, ThemeName name, ThemeDescription description, ThemeImageUrl imageUrl, EntityStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public static Theme create(String name, String description, String imageUrl) {
        return new Theme(null, new ThemeName(name), new ThemeDescription(description), new ThemeImageUrl(imageUrl),
            EntityStatus.ACTIVE);
    }

    public static Theme reconstruct(Long id, String name, String description, String imageUrl, EntityStatus status) {
        return new Theme(id, new ThemeName(name), new ThemeDescription(description), new ThemeImageUrl(imageUrl),
            status);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.value();
    }

    public String getDescription() {
        return description.value();
    }

    public String getImageUrl() {
        return imageUrl.value();
    }

    public EntityStatus getStatus() {
        return status;
    }
}
