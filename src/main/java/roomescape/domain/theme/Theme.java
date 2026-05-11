package roomescape.domain.theme;

public class Theme {

    private Long id;
    private String name;
    private String description;
    private String url;

    public Theme() {
    }

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
}
