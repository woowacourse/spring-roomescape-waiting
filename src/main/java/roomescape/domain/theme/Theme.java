package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.errors.ThemeErrors;

@Table(name = "theme")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Theme {

    private static final int MAX_NAME_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String url;

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
