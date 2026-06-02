package roomescape.theme;

public class Theme {
    private Long id;
    private final String name;
    private final String description;
    private final String image;

    public Theme(String name, String description, String image) {
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public Theme(Long id, String name, String description, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
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

    public String getImage() {
        return image;
    }
}
