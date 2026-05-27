package roomescape.theme.repository.entity;

public class ThemeEntity {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public ThemeEntity(Long id, String name, String description, String thumbnailUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
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
}
