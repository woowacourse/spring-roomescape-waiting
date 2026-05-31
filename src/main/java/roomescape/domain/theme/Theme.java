package roomescape.domain.theme;

import lombok.Getter;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;

@Getter
public class Theme {

    private final Long id;
    private final String name;
    private final String content;
    private final String url;

    private Theme(Long id, String name, String content, String url) {
        validate(name, content, url);
        this.id = id;
        this.name = name;
        this.content = content;
        this.url = url;
    }

    private Theme(String name, String content, String url) {
        this(null, name, content, url);
    }

    public static Theme of(Long id, String name, String content, String url) {
        return new Theme(
            id,
            name,
            content,
            url
        );
    }

    public static Theme createWithoutId(String name, String content, String url) {
        return new Theme(
            name,
            content,
            url
        );
    }

    private static void validate(String name, String content, String url) {
        if (name == null) {
            throw new RoomescapeException(ThemeErrorCode.INVALID_THEME_NAME);
        }
        if (content == null) {
            throw new RoomescapeException(ThemeErrorCode.INVALID_THEME_CONTENT);
        }
        if (url == null) {
            throw new RoomescapeException(ThemeErrorCode.INVALID_THEME_URL);
        }
    }
}
