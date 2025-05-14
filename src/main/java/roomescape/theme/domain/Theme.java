package roomescape.theme.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.BaseEntity;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@ToString
@Entity
@Table(name = "themes")
public class Theme extends BaseEntity {

    @Embedded
    @AttributeOverride(
            name = ThemeName.Fields.value,
            column = @Column(name = Fields.name))
    private ThemeName name;

    @Embedded
    @AttributeOverride(
            name = ThemeDescription.Fields.value,
            column = @Column(name = Fields.description))
    private ThemeDescription description;

    @Embedded
    @AttributeOverride(
            name = ThemeThumbnail.Fields.value,
            column = @Column(name = Fields.thumbnail))
    private ThemeThumbnail thumbnail;

    private Theme(final Long id,
                  final ThemeName name,
                  final ThemeDescription description,
                  final ThemeThumbnail thumbnail) {
        super(id);
        validate(name, description, thumbnail);
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private Theme(final ThemeName name,
                  final ThemeDescription description,
                  final ThemeThumbnail thumbnail) {
        validate(name, description, thumbnail);
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static Theme withId(final ThemeId id,
                               final ThemeName name,
                               final ThemeDescription description,
                               final ThemeThumbnail thumbnail) {
        return new Theme(id.getValue(), name, description, thumbnail);
    }

    public static Theme withoutId(final ThemeName name,
                                  final ThemeDescription description,
                                  final ThemeThumbnail thumbnail) {
        return new Theme(name, description, thumbnail);
    }

    private static void validate(final ThemeName name,
                                 final ThemeDescription description,
                                 final ThemeThumbnail thumbnail) {
        Validator.of(Theme.class)
                .validateNotNull(Fields.name, name, DomainTerm.THEME_NAME.label())
                .validateNotNull(Fields.description, description, DomainTerm.THEME_DESCRIPTION.label())
                .validateNotNull(Fields.thumbnail, thumbnail, DomainTerm.THEME_THUMBNAIL.label());
    }

    public ThemeId getId() {
        return ThemeId.from(id);
    }
}
