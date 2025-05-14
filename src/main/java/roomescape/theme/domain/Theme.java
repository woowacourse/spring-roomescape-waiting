package roomescape.theme.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Theme {

    @EmbeddedId
    private ThemeId id;

    @Embedded
    private ThemeName name;

    @Embedded
    private ThemeDescription description;

    @Embedded
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
