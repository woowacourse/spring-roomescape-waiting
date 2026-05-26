package roomescape.theme.domain;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;

    private Theme(Long id, String name, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public static Theme restore(Long id, String name, String description, String imageUrl) {
        return new Theme(id, name, description, imageUrl);
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
}