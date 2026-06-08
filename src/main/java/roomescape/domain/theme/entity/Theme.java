package roomescape.domain.theme.entity;

import java.time.LocalDateTime;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;
    private final LocalDateTime deletedAt;

    private Theme(Long id, String name, String description, String imageUrl, LocalDateTime deletedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.deletedAt = deletedAt;
    }

    public static Theme create(String name, String description, String imageUrl) {
        return new Theme(null, name, description, imageUrl, null);
    }

    public static Theme reconstruct(Long id, String name, String description, String imageUrl,
        LocalDateTime deletedAt) {
        return new Theme(id, name, description, imageUrl, deletedAt);
    }

    public boolean isSameId(Theme theme) {
        return id.equals(theme.id);
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

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
