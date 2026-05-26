package roomescape.theme.domain;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;

    public Theme(String name, String description, String imageUrl) {
        this(null, name, description, imageUrl);
    }

    private Theme(Long id, String name, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Theme withId(Long id) {
        return new Theme(id, this.name, this.description, this.imageUrl);
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
