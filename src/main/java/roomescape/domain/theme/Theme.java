package roomescape.domain.theme;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public static Theme create(String name, String description, String thumbnailUrl) {
        return new Theme(null, name, description, thumbnailUrl);
    }

    public static Theme of(Long id, String name, String description, String thumbnailUrl) {
        return new Theme(id, name, description, thumbnailUrl);
    }
}
