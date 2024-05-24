package roomescape.service.dto;

import roomescape.model.theme.Description;
import roomescape.model.theme.Name;
import roomescape.model.theme.Theme;
import roomescape.model.theme.Thumbnail;

public class ThemeDto {

    private final Name name;
    private final Description description;
    private final Thumbnail thumbnail;

    public ThemeDto(String name, String description, String thumbnail) {
        this.name = new Name(name);
        this.description = new Description(description);
        this.thumbnail = new Thumbnail(thumbnail);
    }

    public Theme toTheme() {
        return new Theme(this.name, this.description, this.thumbnail);
    }

    public Name getName() {
        return name;
    }

    public Description getDescription() {
        return description;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }
}
