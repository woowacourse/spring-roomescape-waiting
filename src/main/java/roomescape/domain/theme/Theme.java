package roomescape.domain.theme;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String url;

    public Theme(Long id, String name, String description, String url) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public boolean isEqual(Long id) {
        return this.id.equals(id);
    }
}
