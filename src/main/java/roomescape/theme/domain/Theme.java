package roomescape.theme.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@ToString
public class Theme {

    private final ThemeId id;
    private final ThemeName name;
    private final ThemeDescription description;
    private final ThemeThumbnail thumbnail;

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
