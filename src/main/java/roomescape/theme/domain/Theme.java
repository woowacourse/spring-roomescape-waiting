package roomescape.theme.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
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
                .notNullField(Fields.id, id)
                .notNullField(Fields.name, name)
                .notNullField(Fields.description, description)
                .notNullField(Fields.thumbnail, thumbnail);
    }
}
