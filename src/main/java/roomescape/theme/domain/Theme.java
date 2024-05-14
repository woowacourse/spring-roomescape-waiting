package roomescape.theme.domain;

import roomescape.name.domain.Name;

public class Theme {
    private long id;
    private final Name name;
    private final String description;
    private final String thumbnail;

    private Theme(long id, Name name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private Theme(String name, String description, String thumbnail) {
        this(0, new Name(name), description, thumbnail);
    }

    public static Theme themeOf(long id, String name, String description, String thumbnail) {
        return new Theme(id, new Name(name), description, thumbnail);
    }

    public static Theme saveThemeOf(String name, String description, String thumbnail) {
        return new Theme(name, description, thumbnail);
    }

    public static Theme saveThemeFrom(long id) {
        return new Theme(id, null, null, null);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setIdOnSave(long id) {
        this.id = id;
    }

}
