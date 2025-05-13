package roomescape.theme.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@ToString
@Entity
public class Theme {

    @EmbeddedId
    @AttributeOverride(name = ThemeId.Fields.value, column = @Column(name = Fields.id))
    private ThemeId id;

    @Embedded
    @AttributeOverride(name = ThemeName.Fields.value, column = @Column(name = Fields.name))
    private ThemeName name;

    @Embedded
    @AttributeOverride(name = ThemeDescription.Fields.value, column = @Column(name = Fields.description))
    private ThemeDescription description;

    @Embedded
    @AttributeOverride(name = ThemeThumbnail.Fields.value, column = @Column(name = Fields.thumbnail))
    private ThemeThumbnail thumbnail;

    private static Theme of(final ThemeId id,
                            final ThemeName name,
                            final ThemeDescription description,
                            final ThemeThumbnail thumbnail) {
        validate(id, name, description, thumbnail);
        return new Theme(id, name, description, thumbnail);
    }

    public static Theme withId(final ThemeId id,
                               final ThemeName name,
                               final ThemeDescription description,
                               final ThemeThumbnail thumbnail) {
        id.requireAssigned();
        return of(id, name, description, thumbnail);
    }

    public static Theme withoutId(final ThemeName name,
                                  final ThemeDescription description,
                                  final ThemeThumbnail thumbnail) {
        return of(ThemeId.unassigned(), name, description, thumbnail);
    }

    private static void validate(final ThemeId id,
                                 final ThemeName name,
                                 final ThemeDescription description,
                                 final ThemeThumbnail thumbnail) {
        Validator.of(Theme.class)
                .validateNotNull(Fields.id, id, DomainTerm.THEME_ID.label())
                .validateNotNull(Fields.name, name, DomainTerm.THEME_NAME.label())
                .validateNotNull(Fields.description, description, DomainTerm.THEME_DESCRIPTION.label())
                .validateNotNull(Fields.thumbnail, thumbnail, DomainTerm.THEME_THUMBNAIL.label());
    }
}
