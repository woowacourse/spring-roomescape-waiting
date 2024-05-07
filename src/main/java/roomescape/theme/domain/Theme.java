package roomescape.theme.domain;

public class Theme {

    private final String name;
    private final String description;
    private final String thumbnail;
    private long id;

    public Theme(long id) {
        this(id, null, null, null);
    }

    public Theme(String name, String description, String thumbnail) {
        this(0, name, description, thumbnail);
    }

    public Theme(long id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
