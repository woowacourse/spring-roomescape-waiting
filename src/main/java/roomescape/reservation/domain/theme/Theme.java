package roomescape.reservation.domain.theme;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.reservation.domain.util.ValidationUtils;

@Entity
@Getter
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ThemeName themeName;

    @Embedded
    private ThemeDescription themeDescription;

    @Embedded
    private ThemeThumbnail themeThumbnail;

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        validate(name, description, thumbnail);
        this.id = id;
        this.themeName = new ThemeName(name);
        this.themeDescription = new ThemeDescription(description);
        this.themeThumbnail = new ThemeThumbnail(thumbnail);
    }

    private void validate(final String name, final String description, final String thumbnail) {
        ValidationUtils.validateNonNull(name, "테마 이름을 입력해야 합니다.");
        ValidationUtils.validateNonNull(description, "테마 설명을 입력해야 합니다.");
        ValidationUtils.validateNonNull(thumbnail, "테마 썸네일을 입력해야 합니다.");
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
