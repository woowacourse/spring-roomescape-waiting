package roomescape.theme.domain;

import lombok.Getter;
import roomescape.common.exception.BadRequestException;
import roomescape.common.exception.errors.ThemeErrors;

@Getter
public class Theme {

    private static final int MAX_NAME_LENGTH = 10;

    private final Long id;
    private final String name;
    private final String content;
    private final String url;

    private Theme(Long id, String name, String content, String url) {
        validateName(name);
        this.id = id;
        this.name = name;
        this.content = content;
        this.url = url;
    }

    public static Theme of(long id, String name, String content, String url) {
        return new Theme(
            id,
            name,
            content,
            url
        );
    }

    public static Theme createWithoutId(String name, String content, String url) {
        return new Theme(
            null,
            name,
            content,
            url
        );
    }

    private void validateName(String name) {
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException(ThemeErrors.INVALID_THEME_NAME_LENGTH);
        }
    }
}
