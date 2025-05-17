package roomescape.theme.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ThemeName name;

    @Embedded
    private ThemeDescription description;

    @Embedded
    private ThemeThumbnail thumbnail;

    private static Theme of(
            final Long id,
            final ThemeName name,
            final ThemeDescription description,
            final ThemeThumbnail thumbnail
    ) {
        validate(name, description, thumbnail);
        return new Theme(id, name, description, thumbnail);
    }

    public static Theme withId(
            final Long id,
            final ThemeName name,
            final ThemeDescription description,
            final ThemeThumbnail thumbnail
    ) {
        return of(id, name, description, thumbnail);
    }

    public static Theme withoutId(
            final ThemeName name,
            final ThemeDescription description,
            final ThemeThumbnail thumbnail
    ) {
        return of(null, name, description, thumbnail);
    }

    private static void validate(
            final ThemeName name,
            final ThemeDescription description,
            final ThemeThumbnail thumbnail
    ) {
        Validator.of(Theme.class)
                .notNullField(Fields.name, name)
                .notNullField(Fields.description, description)
                .notNullField(Fields.thumbnail, thumbnail);
    }
}
