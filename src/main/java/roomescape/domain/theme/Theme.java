package roomescape.domain.theme;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;

@Entity
@Getter
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String content;
    private String url;

    protected Theme() {

    }

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
