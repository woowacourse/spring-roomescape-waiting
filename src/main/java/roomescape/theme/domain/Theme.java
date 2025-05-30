package roomescape.theme.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Getter
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Theme {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ThemeName themeName;

    @Embedded
    private ThemeDescription themeDescription;

    @Embedded
    private ThemeThumbnail themeThumbnail;

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        this.id = id;
        this.themeName = new ThemeName(name);
        this.themeDescription = new ThemeDescription(description);
        this.themeThumbnail = new ThemeThumbnail(thumbnail);
    }

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public boolean isSameId(final long id) {
        return this.id == id;
    }

    public String getNameOfTheme() {
        return this.themeName.name();
    }

    public String getDescriptionOfTheme() {
        return this.themeDescription.description();
    }

    public String getThumbnailOfTheme() {
        return this.themeThumbnail.thumbnail();
    }
}
